.PHONY: iso-gen
iso-gen:
./mvnw -q -pl libs/iso20022-models -am -P xjc -DskipTests generate-sources