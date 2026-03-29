(ns {{name}}.components
  (:require [common-clj.integrant-components.config :as component.config]
            {{#http-server}}
            [common-clj.integrant-components.routes :as component.routes]
            {{/http-server}}
            {{#http-client}}
            [http-client-component.with-httpkit-client :as component.http-client]
            {{/http-client}}
            {{#telegram}}
            [telegrama.component :as component.telegrama]
            {{/telegram}}
            {{#datalevin}}
            [datalevin.component :as component.datalevin]
            {{/datalevin}}
            [service.component :as component.service]
            {{#datalevin}}
            [{{name}}.db.datalevin.config :as datalevin.config]
            {{/datalevin}}
            {{#http-server}}
            [{{name}}.diplomat.http-server :as diplomat.http-server]
            {{/http-server}}
            {{#telegram}}
            [{{name}}.diplomat.telegram.consumer :as diplomat.telegram.consumer]
            {{/telegram}}
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.tools.logging])
  (:gen-class))

(taoensso.timbre.tools.logging/use-timbre)

(def components
  (merge {:config (ig/ref ::component.config/config)}
         {{#datalevin}}
         {:datalevin (ig/ref ::component.datalevin/datalevin)}
         {{/datalevin}}
         {{#http-client}}
         {:http-client (ig/ref ::component.http-client/http-client)}
         {{/http-client}}))

(def arranjo
  (merge
    {::component.config/config {:path "resources/config.edn"
                                :env  :prod}}
    {{#datalevin}}
    {::component.datalevin/datalevin {:schema     datalevin.config/schema
                                      :components (select-keys components [:config])}}
    {{/datalevin}}
    {{#http-client}}
    {::component.http-client/http-client {:components {:config (ig/ref ::component.config/config)}}}
    {{/http-client}}
    {{#telegram}}
    {::component.telegrama/consumer {:settings   diplomat.telegram.consumer/settings
                                     :components components}}
    {{/telegram}}
    {{#http-server}}
    {::component.routes/routes {:routes diplomat.http-server/routes}}
    {{/http-server}}
    {::component.service/service {:components (merge components
                                                     {{#http-server}}
                                                     {:routes (ig/ref ::component.routes/routes)}
                                                     {{/http-server}})}}))

(defn start-system! []
  (timbre/set-min-level! :debug)
  (ig/init arranjo))

(def -main start-system!)
