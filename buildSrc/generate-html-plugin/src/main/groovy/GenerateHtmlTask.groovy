package com.github.mrjuly.html

import groovy.io.FileType

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.*
import org.thymeleaf.context.Context

import com.eclipsesource.json.*
import groovy.json.JsonSlurper


/**
 * generate static html files from Thymeleaf templates
 */
class GenerateHtmlTask extends DefaultTask {
  File templateRoot = project.file("src/main/webapp/WEB-INF")
  @InputDirectory
  File dataRoot = project.file("src/main/webapp/WEB-INF/data")

  @InputDirectory
  File srcContent = project.file("src/main/webapp/WEB-INF/content")

  @OutputDirectory
  File generatedFileDir = project.file("${project.buildDir}/site")


  def templateEngine = createTemplateEngine()
  // application-wide JSON object (available for each page)
  def globalJson = Json.parse("{}")

  @TaskAction
  public void perform() {
    init()

    processSourceDir(srcContent)
  }


  /**
   * initialization
   */
  def init() {
    def jsonFile = new File("${dataRoot}/app.json")

    if (jsonFile.exists()) {
      globalJson = Json.parse(jsonFile.text)
    }
  }


  /**
   * instantiate the Thymeleaf templating engine
   */
  private TemplateEngine createTemplateEngine() {
    TemplateEngine templateEngine = new TemplateEngine()
    FileTemplateResolver fileTemplateResolver = new FileTemplateResolver()
    fileTemplateResolver.setTemplateMode("HTML")
    fileTemplateResolver.setPrefix("${templateRoot}/")
    fileTemplateResolver.setSuffix(".html")
    templateEngine.setTemplateResolver(fileTemplateResolver)

    return templateEngine
  }


  /**
   * go through specified dir, containing HTML files and process them
   */
  def processSourceDir(root) {
    root.eachDir() { d ->
      processSourceDir(d)
    }
    root.eachFileMatch(~/^[^_].*\.html/) {file ->
      processThymeleafFile(file)
    }
  }


  /**
   * process given Thymeleaf file
   */
  public void processThymeleafFile(f) {
    def template = getTemplateForFile(f)
    println("Processing ${f}")
    Context context = new Context(Locale.US, getDataForTemplate(template))

    getOutputFile(f).write(templateEngine.process(template, context))
  }


  /**
   * get the JSON "back-end" object, available for the specified
   * Thymeleaf template
   */
  def getDataForTemplate(template) {
    def json = Json.parse("{}")
    json.merge(globalJson)
    json.merge(Json.parse("{\"template\":\"${template}\"}"))

    def jf = project.file("${dataRoot}/${template}.json")
    if (jf.exists()) {
      println("Processing JSON: ${jf}")
      json.merge(Json.parse(jf.text))
    }
    def jsonSlurp = new JsonSlurper()

    return jsonSlurp.parseText(json.toString())
  }


  /**
   *  calculate the template name (relative path) for given file.
   */
  public String  getTemplateForFile(f) {
    def filePath = f.absolutePath
    def relativePath = templateRoot.toURI().relativize(f.toURI()).toString()
    return relativePath - ".html"
  }


  /* Creates a file including any directories along the path which do not exist */
  def getOutputFile(f) {
    def filePath = f.absolutePath
    def relativePath = srcContent.toURI().relativize(f.toURI()).toString()

    return  project.file("${generatedFileDir}/${relativePath}")
  }
}
