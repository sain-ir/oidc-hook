#!/bin/bash

fail() { echo -e "\033[1;31mError:\033[0m" "$@" >&2; exit 1; }
warn() { echo -e "\033[1;33mWarning:\033[0m" "$@"; }
info() { echo -e "\033[1;32mInfo:\033[0m" "$@"; }
printHelp() { cat << EOF
Usage: $0 <hostname>
         -keystore <file> [-storepass <password>]
         [-keytool <path>]
         [-quiet] [-test|-testonly]
         [-- <cmd> [args...]]
  <hostname>       - domain from which the SSL certificate should be retrieved
  -keystore <file> - java's keystore file with known certificates
  -storepass <any> - password of keystore file ( default 'changeit' )
  -keytool <path>  - fulll path to keytool ( default 'keytool' )
  -quiet           - do not print any output if -test or -testonly success
  -test            - before updating keystore, test if certificate
                     was changed (by comparing fingerprints SHA1).
                     continue to update keystore only if certificate was changed
  -testonly        - like test, but do not continue update keystore.
                     if certificate changed, returns status code 2.
  <cmd> [args...]  - any command with, optionaly with arguments,
                     which will be executed if certificate
                     was successfully updated in keystore.
EOF
}

H=
K=
P="changeit"
Q=
T=
J=keytool
qwarn() { ! [ -z "$Q" ] || echo -e "\033[1;33mWarning:\033[0m" "$@"; }
qinfo() { ! [ -z "$Q" ] || echo -e "\033[1;32mInfo:\033[0m" "$@"; }
fingerprint() { perl -ne '/(^SHA1 Fingerprint=|fingerprint \(SHA1\): )([A-F\d]{2}(:[A-F\d]{2}){19})/     && print "SHA1 $2";
                          /(^MD5 Fingerprint=|fingerprint \(MD5\): )([A-F\d]{2}(:[A-F\d]{2}){15})/       && print "MD5 $2";
                          /(^SHA256 Fingerprint=|fingerprint \(SHA256\): )([A-F\d]{2}(:[A-F\d]{2}){31})/ && print "SHA256 $2";'; }

while [ $# -gt 0 ]; do
        aux="$1"; shift;
        case "$aux" in
        -h|'-?'|--help) printHelp; exit;;
        -keystore) K="$1"; shift;;
        -keytool) J="$1"; shift;;
        -storepass) P="$1"; shift;;
        -quiet) Q=1;;
        -test) [ -z "$T" ] && T=1 || fail "Only one of parameters -test, -testonly is accepted!";;
        -testonly) [ -z "$T" ] && T=2 || fail "Only one of parameters -test, -testonly is accepted!";;
        --) break;;
        -*) fail "Unknown option \033[35m$aux\033[0m!";;
        *) [ -z "$H" ] && H="$aux" || fail "Too many arguments!";;
        esac
done

hash nslookup 2>/dev/null && ( nslookup "$H" || fail "First parameter ($H) must be a valid hostname!" ) || \
        qwarn "skipping to test domain \033[36m$H\033[0m (because \033[35mnslookup\033[0m is not installed)"
[ -f "$K" ] || fail "Keystore ($K) is not a file!"
[ -w "$K" ] || fail "Unable write to keystore ($K)!"

if ! [ -z "$T" ]; then
        oldF="$("$J" -list -keystore "$K" -storepass "$P" -alias "$H" | fingerprint)"
        newF="$(openssl x509 -in <(openssl s_client -connect "$H":9443 -servername "$H" -prexit < /dev/null 2>/dev/null) \
                -noout -fingerprint ${oldF:+-}${oldF/ */} | fingerprint)"

        [ -z "$newF" ] && fail "Unable to retrieve fingerprint from \033[36m$H\033[0m!"
        [ "$newF" == "$oldF" ] && qinfo "Certificate of \033[36m$H\033[0m was not changed!\n\033[35m  fingerprint ${newF/ /: }\033[0m" && exit
        info "Certificate of \033[36m$H\033[0m was changed:\n\033[31m  old fingerprint ${oldF/ /: }$([ -z "$oldF" ] && echo '<missing>'
                )\n\033[32m  new fingerprint ${newF/ /: }\033[0m"
        [ "$T" -eq 2 ] && exit 2;
fi

"$J" -delete -keystore "$K" -storepass "$P" -alias "$H" && info "Old certificate was removed."
"$J" -noprompt -trustcacerts -importcert -alias "$H" -keystore "$K" -storepass "$P" \
        -file <(openssl x509 -in <(openssl s_client -connect "$H":443 -servername "$H" -prexit < /dev/null 2>/dev/null))
"$J" -list -keystore "$K" -storepass "$P" -alias "$H" && info "Everyting looks fine." || fail "Certificate of $1 was not inserted!"
[ "$#" -gt 0 ] && info "Sarting commnad: \033[36m$@\033[0m" && "$@"
