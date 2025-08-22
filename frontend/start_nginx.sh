#!/bin/bash
if [[ ! -z "${WEBAPP_BASE_URL}" ]]; then
	rm /usr/share/nginx/html/index.html 
	cp /usr/share/nginx/html/index_base.html /usr/share/nginx/html/index.html 
	find '/usr/share/nginx/html' -name 'index.html' -exec sed -i -e 's,<base href="/">,<base href="'"$WEBAPP_BASE_URL"'">,g' {} \; 
fi

if [[ ! -z "${WEBAPP_API_URL}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${WEBAPP_API_URL},'"$WEBAPP_API_URL"',g' {} \; 
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${WEBAPP_API_URL},,g' {} \;
fi

if [[ ! -z "${INSTALLATION_URL}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${INSTALLATION_URL},'"$INSTALLATION_URL"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${INSTALLATION_URL},,g' {} \;
fi

if [[ ! -z "${DEFAULT_CULTURE}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${DEFAULT_CULTURE},'"$DEFAULT_CULTURE"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${DEFAULT_CULTURE},en,g' {} \;
fi

if [[ ! -z "${KEYCLOAK_ADDRESS}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_ADDRESS},'"$KEYCLOAK_ADDRESS"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_ADDRESS},,g' {} \;
fi

if [[ ! -z "${KEYCLOAK_REALM}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_REALM},'"$KEYCLOAK_REALM"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_REALM},,g' {} \;
fi

if [[ ! -z "${KEYCLOAK_CLIENT_ID}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_CLIENT_ID},'"$KEYCLOAK_CLIENT_ID"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_CLIENT_ID},,g' {} \;
fi

if [[ ! -z "${KEYCLOAK_SCOPE}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_SCOPE},'"$KEYCLOAK_SCOPE"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KEYCLOAK_SCOPE},openid profile email address phone api notification annotation identity_provider,g' {} \;
fi

if [[ ! -z "${KPI_SERVICE_ENABLED}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_ENABLED},'"$KPI_SERVICE_ENABLED"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_ENABLED},false,g' {} \;
fi

if [[ ! -z "${KPI_SERVICE_ADDRESS}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_ADDRESS},'"$KPI_SERVICE_ADDRESS"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_ADDRESS},,g' {} \;
fi

if [[ ! -z "${KPI_SERVICE_DASHBOARD_ID}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_DASHBOARD_ID},'"$KPI_SERVICE_DASHBOARD_ID"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_DASHBOARD_ID},,g' {} \;
fi

if [[ ! -z "${KPI_SERVICE_KEYWORD_FILTER}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_KEYWORD_FILTER},'"$KPI_SERVICE_KEYWORD_FILTER"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${KPI_SERVICE_KEYWORD_FILTER},,g' {} \;
fi

if [[ ! -z "${ACCOUNTING_SERVICE_ENABLED}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${ACCOUNTING_SERVICE_ENABLED},'"$ACCOUNTING_SERVICE_ENABLED"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${ACCOUNTING_SERVICE_ENABLED},false,g' {} \;
fi

if [[ ! -z "${LOGGING_ENABLED}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${LOGGING_ENABLED},'"$LOGGING_ENABLED"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${LOGGING_ENABLED},false,g' {} \;
fi

if [[ ! -z "${PRIMARY_COLOR}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${PRIMARY_COLOR},'"$PRIMARY_COLOR"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${PRIMARY_COLOR},,g' {} \;
fi

if [[ ! -z "${CSS_OVERRIDES}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${CSS_OVERRIDES},'"$CSS_OVERRIDES"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${CSS_OVERRIDES},,g' {} \;
fi

if [[ ! -z "${USERWAY_ID}" ]]; then
	find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${USERWAY_ID},'"$USERWAY_ID"',g' {} \;
else
   find '/usr/share/nginx/html/assets/config' -name 'config.json' -exec sed -i -e 's,${USERWAY_ID},,g' {} \;
fi

nginx -g "daemon off;"