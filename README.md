groovy-lint
---

This is for those of use who want to lint groovy files but either can't figure out how or don't feel like using the [annoying java syntax defined by CodeNarc](http://codenarc.sourceforge.net/codenarc-command-line.html).

Installation
---
`curl https://raw.githubusercontent.com/adiktofsugar/groovy-lint/master/remote-install | bash`
This assumes /usr/local/bin is in your PATH. So if it's not, add it, or invoke groovy-lint via `/usr/local/bin/groovy-lint`

Usage:
---

To lint a file or directory with all rules, just:
`groovy-lint path/to/somewhere`

- To watch, add `-w`.
- To change rules, add a `rules.groovy` file to the directory you execute `groovy-lint` from.

NOTES:
---
- Linting a directory will lint every file in the directory with no regard to extension.
- Watching a directory will not pick up new or deleted files.
- rules.groovy sample file found here: http://codenarc.sourceforge.net/StarterRuleSet-AllRulesByCategory.groovy.txt
