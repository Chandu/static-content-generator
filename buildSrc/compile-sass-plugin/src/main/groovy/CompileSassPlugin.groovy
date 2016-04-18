package com.github.mrjuly.sass

import org.gradle.api.Plugin
import org.gradle.api.Project

class CompileSassPlugin implements Plugin<Project> {
  void apply(Project project) {
    // Add the 'sass' extension object
    CompileSassPluginExtension extension =
      project.extensions.create("sass", CompileSassPluginExtension)

    project.afterEvaluate {
      // Add a task that uses the configuration
      project.task('compileSass', type: CompileSassTask,
        group: 'Compilation', description: 'Generate CSS from Sass files') {
        sassDir = project.sass.sassDir
        cssDir = project.sass.cssDir
        importPath = project.sass.importPath
        sasscCmd = project.sass.sasscCmd
      }
    }
  }
}


class CompileSassPluginExtension {
  def sassDir
  def importPath
  def cssDir
  String sasscCmd
}

