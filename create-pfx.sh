#!/bin/bash
NC='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[1;36m'
PURPLE='\033[1;35m'

helpFunction(){
   echo -e "${YELLOW} Usage: $0 -k path -c path -p keyPass -r rfc"
   echo -e "\t-k Path to FIEL Key"
   echo -e "\t-c Path to FIEL Cert"
   echo -e "\t-r FIEL Key Password"
   echo -e "\t-p FIEL Rfc RFC ${NC}"
   exit 1;
}

while getopts "k:c:p:r:" opt
do
   case "$opt" in
      k ) KEY_LOCATION="$OPTARG" ;;
      c ) CERT_LOCATION="$OPTARG" ;;
      r ) RFC="$OPTARG" ;;
      p ) KEY_PASS="$OPTARG" ;;
      ? ) helpFunction ;;
   esac
done

if [ -z "$KEY_LOCATION" ]; then
  echo -e "\n ❌ ${RED} FIEL KEY not set ❌ ${NC}\n"
  helpFunction
fi

if [ -z "$CERT_LOCATION" ]; then
  echo -e "\n ❌ ${RED} FIEL CER not set ❌ ${NC}\n"
  helpFunction
fi

if [ -z "$KEY_PASS" ]; then
  echo -e "\n ❌ ${RED} FIEL KEY pass not set ❌ ${NC}\n"
  helpFunction
fi

if [ -z "$RFC" ]; then
  echo -e "\n ❌ ${RED} FIEL RFC not set ❌ ${NC}\n"
  helpFunction
fi

echo -e "${CYAN} [PFX BUILDER SH]: Creating KEY PEM ${NC}"
openssl pkcs8 -inform der -in $KEY_LOCATION -passin pass:${KEY_PASS} -out key.pem
[ $? -eq 0 ]  || exit 1
echo -e "${CYAN} [PFX BUILDER SH]: Creating CER PEM ${NC}"
openssl x509 -inform der -in $CERT_LOCATION -out cer.pem
[ $? -eq 0 ]  || exit 1
openssl pkcs12 -passout pass:${KEY_PASS} -export -in cer.pem -inkey key.pem -out ./keystore/${RFC}.pfx
[ $? -eq 0 ]  || exit 1
echo -e "${CYAN} [PFX BUILDER SH]: Removing intermediates ${NC}"
rm key.pem && rm cer.pem
[ $? -eq 0 ]  || exit 1
echo -e "${GREEN} [PFX BUILDER SH]: PFX created at: \"./keystore/${RFC}.pfx${NC}\""
