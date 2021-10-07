#!/bin/bash
NC='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[1;36m'
PURPLE='\033[1;35m'

helpFunction(){
   echo -e "${YELLOW} Usage: $0 -k path -c path"
   echo -e "\t-k Path to FIEL Key"
   echo -e "\t-c Path to FIEL Cert"
   echo -e "\t-p FIEL Key Password ${NC}"
   exit 1;
}

while getopts "k:c:p:" opt
do
   case "$opt" in
      k ) KEY_LOCATION="$OPTARG" ;;
      c ) CERT_LOCATION="$OPTARG" ;;
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

echo -e "${CYAN} Creating KEY PEM ${NC}"
openssl pkcs8 -inform der -in $KEY_LOCATION -passin pass:${KEY_PASS} -out key.pem
echo -e "${CYAN} Creating CER PEM ${NC}"
openssl x509 -inform der -in $CERT_LOCATION -out cer.pem
echo -e "${CYAN} Creating PFX. Please add your FIEL KEY Password${NC}"
openssl pkcs12 -export -in cer.pem -inkey key.pem -out src/main/resources/TEST.pfx
echo -e "${CYAN} Removing intermediates ${NC}"
rm key.pem && rm cer.pem
echo -e "${GREEN} your PFX is now in src/main/resources/TEST.pfx ${NC}"

