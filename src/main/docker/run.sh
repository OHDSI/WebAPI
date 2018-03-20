#!/bin/bash

if [[ -n "${KRB5_USER}" ]]; then
    echo "Kerberos init"
    sed -i 's/\(default_realm = \).*/\1'"$KRB5_REALM"'/' /etc/krb5.conf
    sed -i '/\[realms\]/a '"$KRB5_REALM"' = {\n\t\tkdc = '"$KRB5_KDC"'\n\t\tadmin_server = '"$KRB5_ADMIN_SERVER"'\n\t}' /etc/krb5.conf
    echo "#!/usr/bin/env bash
    kinit -R" > /etc/cron.hourly/krb_refresh
    chmod a+x /etc/cron.hourly/krb_refresh

    if [[ -n "${KRB5_PASSWORD}" ]]; then
        echo "${KRB5_PASSWORD}" | kinit "${KRB5_USER}"
    elif [[ -n "${KRB5_KEYTAB}" ]]; then
        kinit -k -t "${KRB5_KEYTAB}" "${KRB5_USER}"
    else
        echo "Either KRB5_PASSWORD or KRB5_KEYTAB is required"
        exit 1
    fi
fi

echo "WEBAPI WILL START"
/opt/tomcat/bin/startup.sh
tail -f /opt/tomcat/logs/catalina.out
exit 0