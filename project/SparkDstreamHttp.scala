import sbt._
import sbt.Keys._
import sbtbuildinfo.Plugin._
import sbtrelease.ReleasePlugin._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._
import scoverage.ScoverageSbtPlugin
import net.virtualvoid.sbt.graph.Plugin._

object SparkDstreamHttp extends Build {

  lazy val basicDependencies: Seq[Setting[_]] = Seq(
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13",
    libraryDependencies += "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "1.2.1",
    libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.2.1",
    libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.4"
  )

  lazy val testDependencies: Seq[Setting[_]] = Seq(
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.1" % "test,it",
    libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % "test,it",
    libraryDependencies += "org.specs2" %% "specs2" % "2.3.8" % "test,it",
    libraryDependencies += "junit" % "junit" % "4.11" % "test,it",
    libraryDependencies += "com.github.simplyscala" %% "simplyscala-server" % "0.5"
  )

  lazy val formattingSettings = FormattingPreferences()
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, false)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
    .setPreference(CompactControlReadability, false)
    .setPreference(CompactStringConcatenation, false)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(FormatXml, true)
    .setPreference(IndentLocalDefs, false)
    .setPreference(IndentPackageBlocks, true)
    .setPreference(IndentSpaces, 2)
    .setPreference(IndentWithTabs, false)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
    .setPreference(PreserveSpaceBeforeArguments, false)
    .setPreference(PreserveDanglingCloseParenthesis, true)
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(SpaceBeforeColon, false)
    .setPreference(SpaceInsideBrackets, false)
    .setPreference(SpaceInsideParentheses, false)
    .setPreference(SpacesWithinPatternBinders, true)

  lazy val itRunSettings = Seq(
    fork in IntegrationTest := true,
    connectInput in IntegrationTest := true
  )

  def vcsNumber: String = {
    val vcsBuildNumber = System.getenv("BUILD_VCS_NUMBER")
    if (vcsBuildNumber == null) "" else vcsBuildNumber
  }

  lazy val forkedJVMOption = Seq("-Duser.timezone=UTC")

  lazy val waterfall = Project(
    id = "spark-dstream-http",
    base = file("."),
    settings = Project.defaultSettings ++ basicDependencies ++ graphSettings ++ releaseSettings ++ scalariformSettingsWithIt ++ itRunSettings ++ testDependencies ++ buildInfoSettings ++ Seq(
      name := "spark-dstream-http",
      organization := "eu.stupidsoup.spark",
      scalaVersion := "2.10.4",
      ScalariformKeys.preferences := formattingSettings,
      ScoverageSbtPlugin.ScoverageKeys.highlighting := true,
      // build info
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name, version),
      buildInfoPackage := "eu.stupidsoup.spark.info",
      // forked JVM
      fork in run := true,
      fork in Test := true,
      fork in testOnly := true,
      javaOptions in run ++= forkedJVMOption,
      javaOptions in Test ++= forkedJVMOption,
      javaOptions in testOnly ++= forkedJVMOption
    )
  ).configs( IntegrationTest )
   .settings( ScoverageSbtPlugin.instrumentSettings ++ Defaults.itSettings ++ Seq(unmanagedSourceDirectories in IntegrationTest <++= { baseDirectory { base => { Seq( base / "src/test/scala" )}}}) : _*)

}
