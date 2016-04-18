package com.github.mrjuly.sass

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;


/**
 * compile Sass files with the aid of jsass.
 * jsass use libsass (native implementation of sass compiler), thus
 * this task will be functional on Windows, Linux, add Mac OS
 */
class CompileSassTask extends DefaultTask {
  @InputDirectory
  def sassDir = null
  @InputFiles
  def importPath = null

  @OutputDirectory
  def cssDir = null

  String sasscCmd = null
  String[] sasscArgs

  Compiler compiler = null
  Options options = null


  @TaskAction
  public void perform() {
    initCompiler()
    processScssDir(sassDir)
  }


  /**
   * Sass compiler initialization
   * TODO: more configuration parameters could be handled here
   * see http://jsass.readthedocs.org/en/latest/options.html
   */
  def initCompiler() {
    if (sasscCmd != null) {
      int ips = importPath != null ? importPath.size() : 0

      sasscArgs = new String[ips * 2 + 3]
      sasscArgs[0] = sasscCmd
    } else {
      compiler = new Compiler()
      options = new Options()
    }
    def argCnt = 1
    if (importPath != null) {
      importPath.each { f ->
        if (sasscCmd != null) {
          sasscArgs[argCnt++] = "-I"
          sasscArgs[argCnt++] = "${f.absolutePath}"
        } else {
          options.getIncludePaths().add(f);
        }
      }
    }
  }


  /**
   * go through specified dir, containing SCSS files and compile them
   */
  def processScssDir(f) {
    f.eachDir() { d ->
      processScssDir(d)
    }
    f.eachFileMatch(~/^[^_].*\.scss/) {file ->
      compileSass(file)
    }
  }


  /**
   * compile given SCSS file
   */
  def compileSass(File f) {
    URI inputFile = f.toURI();
    File oF = getOutputFile(f)
    oF.createNewFile()
    URI outputFile = oF.toURI();

    if (sasscCmd != null) {
      //def cmdToRun = "${sasscCmd} ${sasscArgs} ${f} ${oF}"
      //println "Start sassc: '${cmdToRun}'"
      String inFile = "${f}"
      String outFile = "${oF}"
      def argCnt = sasscArgs.length
      sasscArgs[argCnt - 2] = inFile
      sasscArgs[argCnt - 1] = outFile
      def process = new ProcessBuilder(sasscArgs)
        .inheritIO()
        //.redirectErrorStream(true)
        .start()
      process.waitFor();
      def retVal = process.exitValue()
      if (retVal == 0) {
        println("Compiled successfully");
      } else {
        println("Reurn value = '${retVal}'")
      }
    } else {
      try {
        oF.write(compiler.compileFile(inputFile, outputFile, options).getCss())

        println("Compiled successfully");
      } catch (CompilationException e) {
        println("Compilation of '${inputFile}' to '${outputFile}' failed.");
        println(e.getErrorText());
      }
    }
  }


  /**
   * get the CSS file, which should contain compilation results
   */
  def getOutputFile(f) {
    def filePath = f.absolutePath
    def relativePath = sassDir.toURI().relativize(f.toURI()).toString()
    def newFile = project.file("${cssDir}/${relativePath}" - '.scss' + '.css')
    //newFile.createNewFile()
    //newFile.getParentFile().mkdirs()

    return newFile
  }
}


