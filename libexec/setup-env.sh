default_groovy_home="/usr/local/opt/groovy/libexec"
groovy_home="${GROOVY_HOME:-$default_groovy_home}"

errors=()
if ! [[ -e "$groovy_home" ]]; then
  errors+=("You must set the GROOVY_HOME environment variable to a real directory (wherever you installed groovy)")
else
  groovy_jars=""
  if [[ -e "$groovy_home/embeddable" ]]; then
    for filename in $(ls "$groovy_home/embeddable"); do
      if [[ "$filename" =~ ^groovy-all.+\.jar$ ]]; then
        if ! [[ $filename =~ -indy ]]; then
          groovy_jars=":$groovy_home/embeddable/$filename"
        fi
      fi
    done
  else
    for filename in $(ls "$groovy_home/lib"); do
      if [[ "$filename" =~ ^groovy.+\.jar$ ]]; then
        groovy_jars+=":$groovy_home/lib/$filename"
      fi
    done
  fi
  if ! [[ "$groovy_jars" ]]; then
    errors+=("groovy must be installed. found no groovy jar in $groovy_home")
  fi
fi

if ! which java >/dev/null 2>/dev/null; then
  errors+=("You must have java installed")  
fi

if [[ ${#errors} -gt 0 ]];then
  for error in errors; do
    echo "$error" >&2
  done
  exit 1
fi

classpath_lib="$project_root/lib"
cn_jar="$classpath_lib/CodeNarc-1.2.jar"
slf_api_jar="$classpath_lib/slf4j-api-1.7.25.jar"
slf_simple_jar="$classpath_lib/slf4j-simple-1.7.25.jar"
jansi_jar="$classpath_lib/jansi-1.17.1.jar"
cli_jar="$classpath_lib/commons-cli-1.4.jar"
groovy_jars="${groovy_jars##:}" # remove the : from the front

# minimum for compile
classpath="$cn_jar:$slf_api_jar:$slf_simple_jar:$jansi_jar:$cli_jar:$groovy_jars"

