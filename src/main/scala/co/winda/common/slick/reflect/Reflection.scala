package co.winda.common.slick.reflect

import scala.reflect._
import org.reflections.util._
import org.reflections.scanners
import org.reflections.scanners.TypeElementsScanner

import scala.reflect.runtime.universe._
import org.reflections.{Configuration, Reflections}

/**
* @author David Karigithu
* @since 04-06-2016
*/
object Reflection {
  import annotation.tailrec

  /**
  * Get Model Reflections
  * @param classloader ClassLoader
  * @param pkg String
  * @return Option[Reflections]
  */
  def getReflections(classloader: ClassLoader, pkg: String): Option[Reflections] = {
    val scanUrls = org.reflections.util.ClasspathHelper.forPackage(pkg, classloader)
    if (!scanUrls.isEmpty)
      Some(new Reflections(new ConfigurationBuilder()
        .addUrls(scanUrls)
        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg + ".")))
        .setScanners(new scanners.TypeAnnotationsScanner, new scanners.TypeElementsScanner)))
    else
      None
  }

  def splitIdentifiers(names: String) = names.split("""\.""").filter(!_.trim.isEmpty).toList
  private def assembleIdentifiers(ids: Seq[String]) = ids.mkString(".")

  /**
  * Find first
  * @param names String
  * @param mirror JavaMirror
  * @return Option[ModuleSymbol]
  */
  def findFirstModule(names: String)(implicit mirror: JavaMirror): Option[ModuleSymbol] = {

    // Check if a given ident is a module
    def symbolForIdent(ident: String): Option[ModuleSymbol] = {
      try {
        val symbol = mirror.staticModule(ident)
        mirror.reflectModule(symbol).instance //if we can reflect a module it means we are module
        Some(symbol)
      } catch {
        case _: ScalaReflectionException => None
        case _: reflect.internal.MissingRequirementError => None
        //FIXME: must be another way to check if a static modules exists than exceptions!?!
        case _: ClassNotFoundException => None
        //We tried to reflect a module but got a class cast exception (again, would be nice to do this differently)
      }
    }

    // Produce a sequence of all the identifier prefixes of the given name
    val identifierSeqs: Seq[Seq[String]] = splitIdentifiers(names).scanLeft(Seq.empty[String]) {
      case (prefix, elem) => prefix :+ elem
    }.drop(1)

    // Get the first matching module, if any
    identifierSeqs.foldLeft(Option.empty[ModuleSymbol]) {
      case (found@Some(_), _) => found
      case (None, identifier) => symbolForIdent(assembleIdentifiers(identifier))
    }
  }

  /**
  * Get Module/Field from Mirror
  * @param name String
  * @param base Any
  * @param baseSymbol Symbol
  * @param mirror JavaMirror
  * @return (Symbol, Any)
  */
  def reflectModuleOrField(name: String, base: Any, baseSymbol: Symbol)(implicit mirror: JavaMirror): (Symbol, Any) = {
    val baseIM = mirror.reflect(base)
    val baseMember = baseSymbol.typeSignature.member(newTermName(name))
    val instance = if (baseMember.isModule) {
      if (baseMember.isStatic) {
        mirror.reflectModule(baseMember.asModule).instance
      } else {
        baseIM.reflectModule(baseMember.asModule).instance
      }
    } else {
      assert(baseMember.isTerm, s"Expected '$name' to be something that can be reflected on $base as a field")
      baseIM.reflectMethod(baseMember.asTerm.asMethod).apply()
    }
    baseMember -> instance
  }

  /**
  * Scan Module/Field by Reflection
  * @param baseSym Symbol
  * @param sym Symbol
  * @param instance Any
  * @param checkSymbol Symbol => Boolean
  * @param mirror List[(Symbol, Any)]
  * @return
  */
  def scanModuleOrFieldByReflection(baseSym: Symbol, sym: Symbol, instance: Any)(checkSymbol: Symbol => Boolean)(implicit mirror: JavaMirror): List[(Symbol, Any)] = {
    val rootPackage = baseSym.fullName

    val extractMembers: PartialFunction[(Symbol, Any), Iterable[(Symbol, Any)]] = {
      case (baseSym, baseInstance) if (baseInstance != null) =>
        val members = baseSym.typeSignature.members
        val vals = members.filter(s => s.isModule || (s.isTerm && s.asTerm.isAccessor))

        vals.flatMap { mSym =>
          try {
            List(reflectModuleOrField(mSym.name.decodedName.toString, baseInstance, baseSym))
          } catch {
            case _: ScalaReflectionException => List.empty
          }
        }
      case _ => List.empty
    }

    /**
    * Scan Modules/Fields by Reflection
    * @param found
    * @param checked
    * @param instancesNsyms
    * @return
    */
    @tailrec
    def _scanModuleOrFieldByReflection(found: List[(Symbol, Any)], checked: Set[Symbol], instancesNsyms: List[(Symbol, Any)]): List[(Symbol, Any)] = {
      val members = instancesNsyms.flatMap(extractMembers)
      val (matching, possibleCandidates) = members.partition { case (symb, _) => checkSymbol(symb) }
      val candidates = possibleCandidates
        .filter { case (symb, instance) => !checked.contains(symb) && symb.fullName.startsWith(rootPackage) }

      if (candidates.isEmpty)
        (found ++ matching).distinct
      else
        _scanModuleOrFieldByReflection(found ++ matching, checked ++ (matching ++ candidates).map(_._1), candidates)
    }

    _scanModuleOrFieldByReflection(List.empty, Set.empty, List(sym -> instance))
  }

}