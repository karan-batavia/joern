package io.joern.rubysrc2cpg.testfixtures

import io.joern.dataflowengineoss.language.Path
import io.joern.dataflowengineoss.layers.dataflows.{OssDataFlow, OssDataFlowOptions}
import io.joern.dataflowengineoss.queryengine.EngineContext
import io.joern.rubysrc2cpg.deprecated.utils.PackageTable
import io.joern.rubysrc2cpg.{Config, RubySrc2Cpg}
import io.joern.x2cpg.testfixtures.*
import io.joern.x2cpg.{ValidationMode, X2Cpg}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.semanticcpg.language.{ICallResolver, NoResolve}
import io.shiftleft.semanticcpg.layers.LayerCreatorContext
import org.scalatest.Tag

import java.io.File

trait RubyFrontend(useDeprecatedFrontend: Boolean) extends LanguageFrontend {
  override val fileSuffix: String = ".rb"

  implicit val config: Config =
    getConfig()
      .map(_.asInstanceOf[Config])
      .getOrElse(Config().withSchemaValidation(ValidationMode.Enabled))
      .withUseDeprecatedFrontend(useDeprecatedFrontend)

  override def execute(sourceCodeFile: File): Cpg = {
    new RubySrc2Cpg().createCpg(sourceCodeFile.getAbsolutePath).get
  }

}

class DefaultTestCpgWithRuby(
  withPostProcessing: Boolean,
  withDataFlow: Boolean,
  packageTable: Option[PackageTable],
  useDeprecatedFrontend: Boolean
) extends DefaultTestCpg
    with RubyFrontend(useDeprecatedFrontend) {

  override def applyPasses(): Unit = {
    X2Cpg.applyDefaultOverlays(this)

    if (withPostProcessing) {
      packageTable match {
        case Some(table) =>
          RubySrc2Cpg.packageTableInfo.set(table)
        case None =>
      }
      RubySrc2Cpg.postProcessingPasses(this, config).foreach(_.createAndApply())
    }

    if (withDataFlow) {
      val context = new LayerCreatorContext(this)
      val options = new OssDataFlowOptions()
      new OssDataFlow(options).run(context)
    }
  }

}

class RubyCode2CpgFixture(
  withPostProcessing: Boolean = false,
  withDataFlow: Boolean = false,
  packageTable: Option[PackageTable] = None,
  useDeprecatedFrontend: Boolean = false
) extends Code2CpgFixture(() =>
      new DefaultTestCpgWithRuby(withPostProcessing, withDataFlow, packageTable, useDeprecatedFrontend)
    ) {

  implicit val resolver: ICallResolver           = NoResolve
  implicit lazy val engineContext: EngineContext = EngineContext()

  protected def flowToResultPairs(path: Path): List[(String, Integer)] =
    path.resultPairs().collect { case (firstElement: String, secondElement: Option[Integer]) =>
      (firstElement, secondElement.get)
    }
}

class RubyCfgTestCpg(useDeprecatedFrontend: Boolean = true)
    extends CfgTestCpg
    with RubyFrontend(useDeprecatedFrontend) {
  override val fileSuffix: String = ".rb"

}

/** Denotes a test which has been similarly ported to the new frontend.
  */
object SameInNewFrontend extends Tag("SameInNewFrontend")

/** Denotes a test which has been ported to the new frontend, but has different expectations.
  */
object DifferentInNewFrontend extends Tag("DifferentInNewFrontend")
