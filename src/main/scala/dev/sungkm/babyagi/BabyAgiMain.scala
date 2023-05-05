package dev.sungkm.babyagi

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp}

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import pureconfig.*
import pureconfig.generic.derivation.default.*
import sttp.client3.*
import sttp.client3.jsoniter.*
import sttp.client3.armeria.cats.ArmeriaCatsBackend

final case class BabyAgiConf(
    openApiKey: String,
    pineconeApiKey: String,
    pineconeEnvironment: String,
    yourTableName: String,
    objective: String,
    yourFirstTask: String,
) derives ConfigReader

object BabyAgiMain extends IOApp:
  def run(args: List[String]): IO[ExitCode] =

    val conf = ConfigSource.default.loadOrThrow[BabyAgiConf]

    ArmeriaCatsBackend
      .resource[IO]()
      .use: backend =>
        val listIndexesRequest = basicRequest
          .get(
            uri"https://controller.${conf.pineconeEnvironment}.pinecone.io/databases",
          )
          .header("Api-Key", conf.pineconeApiKey)

        case class CreateIndexRequest(
            name: String,
            dimension: Int,
            metric: String,
            podType: String,
        )
        given createIndexJsonCodec: JsonValueCodec[CreateIndexRequest] =
          JsonCodecMaker.make
        def createIndexRequest(request: CreateIndexRequest) = basicRequest
          .post:
            uri"https://controller.${conf.pineconeEnvironment}.pinecone.io/databases"
          .header("Api-Key", conf.pineconeApiKey)
          .body(request)

        val program = for
          indexes <- EitherT(listIndexesRequest.send(backend).map(_.body))
          response <-
            if indexes contains conf.yourTableName then
              EitherT.rightT[IO, String]("Table already exists")
            else
              EitherT(
                createIndexRequest(
                  CreateIndexRequest(
                    name = conf.yourTableName,
                    dimension = 1536,
                    metric = "cosine",
                    podType = "p1",
                  ),
                ).send(backend).map(_.body),
              )
        yield response

        program.value.map:
          case Right(response) =>
            scribe.info(response)
            ExitCode.Success
          case Left(error) =>
            scribe.error(error)
            ExitCode.Error
