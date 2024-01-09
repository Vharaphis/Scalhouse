# Class Exam Instruction: Building a ZIO Streams Application

## Objective
The objective of this exam is to create a robust ZIO Streams-based application that applies various functional programming concepts covered during the course. This includes immutability, case classes, new types, pattern matching, recursion, and more.

## Task Overview
Build an application that retrieves, processes, and analyzes data from a chosen source using ZIO Streams. The source of data can be a public API, local files, or any relevant data that aligns with the chosen theme.

## Requirements

1. Functional Programming Concepts Integration:
    * Demonstrate immutability by designing data structures and operations that emphasize immutable values.
    * Utilize case classes to model data entities, capturing relevant attributes and functionalities.
    * Create new types to encapsulate domain-specific logic and enforce type safety.
    * Implement pattern matching to handle different cases and efficiently process data.
    * Utilize recursion where appropriate to perform operations or traverse data structures.
1. ZIO Streams Implementation:
    * Utilize ZIO Streams for handling data processing and manipulation.
    * Implement stream transformations, such as mapping, filtering, and aggregating, to showcase ZIO Streams' capabilities.
    * Demonstrate error handling and resource management using ZIO's effectful streaming.
    * Use other libraries from the ZIO ecosystem if relevant, such as `zio-jdbc`,`zio-json`, or `zio-http` to handle database operations, JSON parsing, and HTTP application, respectively.
1. Source Data Selection:
    * Choose a relevant source for the data to be processed. Options include:
        * Public APIs providing real-time or static data (e.g., weather data, financial data, etc.).
        * Local files containing diverse and substantial data.
        * Any other data sources relevant to the theme (ensure it allows for ample demonstration of functional programming concepts).
1. Application Functionality:
    * Design and implement functionality to process and analyze the retrieved data.
    * Showcase the application's capability to perform meaningful operations (e.g., filtering, aggregation, transformation, etc.).
    * Generate informative output or visualization based on processed data.
    * Write test cases to validate the functionality of your application.
    * Be creative and explain your motivation in your project README file.
1. Code Quality and Documentation:
    * Write clean, well-documented code that reflects best practices of functional programming.
    * Include comments where necessary to explain complex logic or important design decisions.
    * Ensure readability, maintainability, and adherence to functional programming principles
1. Git repository and documentation quality:
    * Set up a Git repository to manage your application's source code
    * Ensure that your repository is well-organized, contains appropriate commits, and has a clear README file.
    * Document your code, including class and method-level comments, explaining the purpose and functionality of each component.

## Additional Requirements

1. Group Size: Form groups of up to 4 students. You are encouraged to collaborate and discuss ideas within your group but ensure that each member actively contributes to the project.

1. Due Date: The project is expected to be completed and submitted by **Friday evening, January 12, 2024**. Late submissions may incur penalties unless prior arrangements have been made with the instructor.

1. Language: Use English for code, comments and documentation.

1. Plagiarism or using code from external sources without proper attribution is strictly prohibited.

1. Feel free to consult course materials, documentation, or relevant resources while working on the exam.

## Deliverables

1. Scala 3 code implementing the ZIO application, adhering to the given requirements and expectations.

1. Git repository containing your code with appropriate commits and a README file providing use case presentation, a functional schema, instructions on how to run and test your application and the decisions made (libraries, data structure(s), algorithm, performance, ...).

1. Documentation explaining the purpose, functionality, and usage of your application, along with any external libraries used.

## Grading

Your solution will be graded based on the following criteria, with equal distribution of the number of points on **criteria 1 to 5**, and against the `main` or `master` branch of your repository unless specific instruction in your README file:

1. Effective usage of ZIO Streams, including other libraries from the ZIO ecosystem.

1. Correctness and functionality of the application implementation.

1. Adherence to functional programming principles.

1. Testing completeness and effectiveness, covering various scenarios.

1. Quality and clarity of code organization and documentation, including the README file.

1. Collaboration within the group and active participation of each member.

1. Timely submission of the project by the specified due date.

**Take your time discovering the ZIO ecosystem by reading the official documentation.**

Good luck with your exam, and feel free to ask any further questions!

---

## Annex: ZIO Application Example

The complete code below provides a complete Scala project and ZIO application. When building your application, you can reuse some of the components. Please review Scala and other dependencies version number to be up to date.

### Data Model

First, we are defining a `case class` to represents games. Your solution should include scores, predictions, ... In this example, we are using opaque types, but this is optional.

```scala
final case class Game(
    date: GameDate,
    season: SeasonYear,
    playoffRound: Option[PlayoffRound],
    homeTeam: HomeTeam,
    awayTeam: AwayTeam
)
```

In the `Game` companion object, we are providing encoders and decoders for JSON and JDBC.

```scala
object Game {

  given CanEqual[Game, Game] = CanEqual.derived
  implicit val gameEncoder: JsonEncoder[Game] = DeriveJsonEncoder.gen[Game]
  implicit val gameDecoder: JsonDecoder[Game] = DeriveJsonDecoder.gen[Game]

  def unapply(game: Game): (GameDate, SeasonYear, Option[PlayoffRound], HomeTeam, AwayTeam) =
    (game.date, game.season, game.playoffRound, game.homeTeam, game.awayTeam)

  // a custom decoder from a tuple
  type Row = (String, Int, Option[Int], String, String)

  extension (g:Game)
    def toRow: Row =
      val (d, y, p, h, a) = Game.unapply(g)
      (
        GameDate.unapply(d).toString,
        SeasonYear.unapply(y),
        p.map(PlayoffRound.unapply),
        HomeTeam.unapply(h),
        AwayTeam.unapply(a)
      )

  implicit val jdbcDecoder: JdbcDecoder[Game] = JdbcDecoder[Row]().map[Game] { t =>
      val (date, season, maybePlayoff, home, away) = t
      Game(
        GameDate(LocalDate.parse(date)),
        SeasonYear(season),
        maybePlayoff.map(PlayoffRound(_)),
        HomeTeam(home),
        AwayTeam(away)
      )
    }
}
```

### Database Layer

To be able to interact with the database, we first need to create a datasource, called `ZConnectionPool` in ZIO JDBC. We are using the default pool configuration, defining a user and a password and configuring a `mlb` in memory.

```scala
val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
  ZLayer.succeed(ZConnectionPoolConfig.default)

val properties: Map[String, String] = Map(
  "user" -> "postgres",
  "password" -> "postgres"
)

val connectionPool: ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
  ZConnectionPool.h2mem(
    database = "mlb",
    props = properties
  )
```

Then, we can define some queries, like `CREATE TABLE`, `INSERT` or `SELECT`. Note that every queries results are of type `ZIO`, with `ZConnectionPool` as environment (dependency) and `Throwable` as effect type in case of errors.

```scala
val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"CREATE TABLE IF NOT EXISTS games(date DATE NOT NULL, season_year INT NOT NULL, playoff_round INT, home_team VARCHAR(3), away_team VARCHAR(3))"
    )
  }

val insertRows: ZIO[ZConnectionPool, Throwable, UpdateResult] = {
  val rows: List[Game.Row] = games.map(_.toRow)
  transaction {
    insert(
      sql"INSERT INTO games(date, season_year, playoff_round, home_team, away_team)".values[Game.Row](rows)
    )
  }
}

val count: ZIO[ZConnectionPool, Throwable, Option[Int]] = transaction {
  selectOne(
    sql"SELECT COUNT(*) FROM games".as[Int]
  )
}
```

### HTTP Endpoints

You can configure static endpoints like:

```scala
val static: App[Any] = Http.collect[Request] {
  case Method.GET -> Root / "text" => Response.text("Hello MLB Fans!")
  case Method.GET -> Root / "json" => Response.json("""{"greetings": "Hello MLB Fans!"}""")
}.withDefaultErrorResponse
```

Or integrate your database layer and application logic in dynamic endpoints. In the example below, you can see that or depedency to `ZConnectionPool` is made explicit by the type `App[ZConnectionPool]`.

```scala
val endpoints: App[ZConnectionPool] = Http.collectZIO[Request] {
  case Method.GET -> Root / "games" / "count" =>
    for {
      count: Option[Int] <- count
      res: Response = countResponse(count)
    } yield res
  case _ =>
    ZIO.succeed(Response.text("Not Found").withStatus(Status.NotFound))
}.withDefaultErrorResponse
```

Once our endpoints are defined, we can declare the globale application logic and its dependencies, `ZConnectionPool` and `Server` (for HTTP server). In the example, we are creating the table first, inserting the sample data and the configuring the server routes with both static and dynamic endpoints.

```scala
val appLogic: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
  _ <- create *> insertRows
  _ <- Server.serve[ZConnectionPool](static ++ endpoints)
} yield ()
```

Finaly, we are overriding the `run` method of the `ZIOAppDefault` class:

```scala
override def run: ZIO[Any, Throwable, Unit] =
  appLogic.provide(createZIOPoolConfig >>> connectionPool, Server.default)
```

### Build Configuration

Here's the associated `build.sbt` file for the skeleton code provided above:

```scala
val scala3Version = "3.3.0"
val h2Version = "2.1.214"
val scalaCsvVersion = "1.3.10"
val zioVersion = "2.0.6"
val zioSchemaVersion = "0.4.8"
val zioJdbcVersion = "0.0.2"
val zioJsonVersion = "0.5.0"
val zioHttpVersion = "3.0.0-RC2"

lazy val root = (project in file("."))
  .settings(
    name := "mlb-api",
    version := "1.0",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % h2Version,
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-schema" % zioSchemaVersion,
      "dev.zio" %% "zio-jdbc" % zioJdbcVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "dev.zio" %% "zio-http" % zioHttpVersion,
      "com.github.tototoshi" %% "scala-csv" % scalaCsvVersion,
    ).map(_ % Compile),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29"
    ).map(_ % Test)
  )
```

Make sure to place this `build.sbt` file in the root directory of your project. Adjust the dependencies' versions as necessary, and add any additional dependencies required for your project.

This is a basic `build.sbt` configuration. Depending on your project's requirements, you may need to add more settings, such as resolvers, additional libraries, plugins or configurations for code formatting, coverage, and more.

### Streams

In the example above, `insertRows` is very simple and takes no parameter. You may want to make it a function and use a stream to batch insert your data at initialization time.

```scala
for {
  conn <- create
  source <- ZIO.succeed(CSVReader.open(???))
  stream <- ZStream
    .fromIterator[Seq[String]](source.iterator)
    .map[Option[Game]](???)
    .collectSome[Game]
    .grouped(???)
    .foreach(chunk => insertRows(???))
  _ <- ZIO.succeed(source.close())
  res <- select
} yield res
```

### Running and Testing

Finally, it is encouraged to use [sbt-revolver](https://github.com/spray/sbt-revolver) in your workflow. This is a plugin for SBT enabling a super-fast development turnaround for your Scala applications. It supports the following features:
* Starting and stopping your application in the background of your interactive SBT shell (in a forked JVM).
* Triggered restart: automatically restart your application as soon as some of its sources have been changed.

Add the following dependency to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")
```

You can then use `~reStart` to go into "triggered restart" mode. Your application starts up and SBT watches for changes in your source (or resource) files. If a change is detected SBT recompiles the required classes and sbt-revolver automatically restarts your application. When you press `<ENTER>` SBT leaves "triggered restart" and returns to the normal prompt keeping your application running.

Compile then run project with auto reload thanks to sbt-revolver:
``` bash
$ sbt
[...]
sbt:mlb-api> compile
sbt:mlb-api> ~reStart
```

This will create a server, listening on 8080 by default. Test your API with a tool like Postman or the `curl`:

```bash
$ curl -s -D - -o /dev/null "http://localhost:8080/text"
```
