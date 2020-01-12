import org.codenarc.analyzer.SourceAnalyzer
import org.codenarc.analyzer.StringSourceAnalyzer
import org.codenarc.results.VirtualResults
import org.codenarc.results.FileResults
import org.codenarc.ruleregistry.RuleRegistryInitializer
import org.codenarc.ruleset.PropertiesFileRuleSetConfigurer
import org.codenarc.ruleset.RuleSet
import org.codenarc.ruleset.RuleSetUtil
import org.codenarc.AnalysisContext
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.nio.file.Files
import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

@SuppressWarnings(['Println', 'PrintStackTrace'])
class CliCodeNarc {

  private Boolean shouldWatch
  private RuleSet ruleSet
  private String[] filepaths
  private Map<WatchKey,String> keyToParentPath

  static void main(String[] args) {
    Boolean shouldWatch = (args.first() =~ /true$/)
    def codeNarc = new CliCodeNarc(shouldWatch)
    try {
      AnsiConsole.systemInstall();
      codeNarc.execute(args.drop(1))
      AnsiConsole.systemUninstall();
    }
    catch(Throwable t) {
      println "ERROR: ${t.message}"
      t.printStackTrace()
      System.exit(1)
    }
  }

  public CliCodeNarc(Boolean shouldWatch) {
    this.shouldWatch = shouldWatch
  }

  

  protected void execute(String[] filepathIndicators) {
    if (filepathIndicators.size() == 0) {
      println "No files passed. Leaving."
      return
    }
    filepaths = expandFilepaths(filepathIndicators)
    initializeRuleSet()
    filepaths.each { analyzeFile(it) }

    if (!shouldWatch) {
      return
    }
    keyToParentPath = new HashMap<WatchKey,String>()
    def watchService = FileSystems.getDefault().newWatchService()
    filepaths.each {
      def parentPath = new File(it).toPath().getParent()
      if (parentPath) {
        WatchKey key = parentPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        keyToParentPath.put(key, parentPath)
        println ansi().fg(GREEN).a("Watching ${parentPath} for ${it}").reset()
      } else {
        println ansi().fg(RED).a("Cannot watch ${it} because it has no parent").reset()
      }
    }

    while (true) {
      def key = watchService.take()
      if (key) {
        String parentPath = keyToParentPath.get(key)
        key.pollEvents()
          .collect {
            String childPath = it.context().toString()
            new File(parentPath, childPath).toPath().toString()
          }
          .each {
            println ansi().fg(GREEN).a("${it} changed").reset()
            if (filepaths.contains(it)) {
              analyzeFile(it)
            }
          }
        key.reset()
      }
    }
  }

  private void addFilepaths(ArrayList<String> filepaths, String filepath) {
    def file = new File(filepath)
    if (file.isDirectory()) {
      file.listFiles().each {
        addFilepaths(filepaths, it.toPath().toString())
      }
    } else {
      filepaths << file.toPath().toRealPath().toString()
    }
  }
  private String[] expandFilepaths(String[] filepathIndicators) {
    def filepaths = []
    filepathIndicators.each {
      addFilepaths(filepaths, it)
    }
    return filepaths
  }

  private void analyzeFile(String filepath) {
    def file = new File(filepath)
    if (file.canRead()) {
      String source = file.getText('UTF-8')
      SourceAnalyzer sourceAnalyzer = new StringSourceAnalyzer(source)
      VirtualResults virtualResults = sourceAnalyzer.analyze(ruleSet)
      FileResults results = new FileResults(filepath, virtualResults.getViolations())
      AnalysisContext analysisContext = new AnalysisContext(ruleSet:ruleSet, sourceDirectories:sourceAnalyzer.sourceDirectories)
      new CliConsoleReportWriter().writeReport(analysisContext, results)
    }
  }

  private void initializeRuleSet() {
    new RuleRegistryInitializer().initializeRuleRegistry()
    // This looks for any rules.groovy on the classpath, so you can use your own if it's called rules.groovy and
    //  on the classpath
    ruleSet = RuleSetUtil.loadRuleSetFile('rules.groovy')
  }
}
