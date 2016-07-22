package com.github.mrjuly.html

import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateHtmlPlugin implements Plugin<Project> {
  void apply(Project project) {
    // Add the 'htmlGenerator' extension object
    GenerateHtmlPluginExtension extension =
    project.extensions.create("htmlGenerator", GenerateHtmlPluginExtension)

    project.afterEvaluate {
      // Add a task that uses the configuration
      project.task('generateHtml', type: GenerateHtmlTask,
        group: 'Compilation', description: 'Generate HTML from Thymeleaf templates') {
        templateRoot = project.htmlGenerator.templateRoot
        dataRoot = project.htmlGenerator.dataRoot
        srcContent = project.htmlGenerator.srcContent
        generatedFileDir = project.htmlGenerator.generatedFileDir
      }
    }
  }
}


class GenerateHtmlPluginExtension {
  def templateRoot
  def dataRoot
  def srcContent
  def generatedFileDir
}

