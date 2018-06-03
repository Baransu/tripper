name := "todo"

version := "0.1"

scalaVersion := "2.12.6"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("hseeberger", "maven")
)

libraryDependencies ++= {
  val AkkaVersion = "2.5.9"
  val AkkaHttpVersion = "10.0.11"
  val CirceVersion = "0.9.3"
  val Json4sVersion = "3.5.4"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % AkkaVersion,

    "org.sangria-graphql" %% "sangria" % "1.4.0",
    "org.sangria-graphql" %% "sangria-circe" % "1.2.1",
    "org.sangria-graphql" %% "sangria-json4s-native" % "1.0.0",

    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core"   % AkkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.21.0",
    "org.json4s" %% "json4s-native" % Json4sVersion,
    "org.json4s" %% "json4s-core"   % Json4sVersion,

    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
}


