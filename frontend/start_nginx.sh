#!/bin/bash
if [[ ! -z "${WEBAPP_BASE_URL}" ]]; then
	rm /usr/share/nginx/html/index.html 
	cp /usr/share/nginx/html/index_base.html /usr/share/nginx/html/index.html 
	find '/usr/share/nginx/html' -name 'index.html' -exec sed -i -e 's,<base href="/">,<base href="'"$WEBAPP_BASE_URL"'">,g' {} \; 
fi

nginx -g "daemon off;"