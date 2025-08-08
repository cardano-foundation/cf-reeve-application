#!/bin/bash

native_image_agent_dest="../cf-application/src/main/resources/META-INF/native-image"
resource_config_json="${native_image_agent_dest}/resource-config.json"

# echo "copy agent files to cf-application resources"
# cp -r META-INF/native-image/* $native_image_agent_dest

echo 'remove \QMETA-INF/services/org.hibernate.bytecode.spi.BytecodeProvider\E' "in $resource_config_json"
echo "  https://github.com/spring-projects/spring-framework/issues/35118 for more information."
jq --arg pat '\QMETA-INF/services/org.hibernate.bytecode.spi.BytecodeProvider\E' \
    '.resources.includes |= map(select(.pattern != $pat))' $resource_config_json > /tmp/resource-config.json
mv /tmp/resource-config.json $resource_config_json
