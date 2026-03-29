(ns leiningen.new.microservice
  (:require [leiningen.new.templates :as tmpl]
            [leiningen.core.main :as main]
            [clojure.string :as str]))

(def render (tmpl/renderer "microservice"))

(def all-components #{:datalevin :http-server :telegram :http-client})

; Component hard dependencies: if key is enabled, all values must also be enabled
(def component-deps
  {:telegram #{:http-client}})

(defn parse-flags [flag-args]
  (let [opt-in?  (some #(str/starts-with? % "+") flag-args)
        defaults (zipmap all-components (repeat (not opt-in?)))]
    (reduce
      (fn [acc flag]
        (let [[_ sign component-name] (re-matches #"([+-])(.*)" flag)]
          (if (nil? sign)
            acc
            (let [component (keyword component-name)]
              (if (contains? all-components component)
                (assoc acc component (= sign "+"))
                (do (main/warn (str "Unknown component flag: " flag)) acc))))))
      defaults
      flag-args)))

(defn- auto-enable [flags component dep]
  (if (flags dep)
    flags
    (do (main/info (str (name component) " requires " (name dep) " — enabling it automatically"))
        (assoc flags dep true))))

(defn resolve-deps
  "Enforces component dependencies. If a component is enabled, its required
  dependencies are auto-enabled with an info message."
  [flags]
  (reduce (fn [flags-acc [component required-deps]]
            (if (flags-acc component)
              (reduce #(auto-enable %1 component %2) flags-acc required-deps)
              flags-acc))
          flags
          component-deps))

(defn microservice
  "Generate a Clojure microservice. Use +/- flags to include/exclude components:
   datalevin, http-server, telegram, http-client (all included by default)"
  [& args]
  (let [name  (first args)
        flags (-> (rest args) parse-flags resolve-deps)
        data  (merge {:name      name
                      :sanitized (tmpl/name-to-path name)}
                     flags)]
    (main/info "Generating fresh 'lein new' net.clojars.macielti/microservice project.")
    (main/info "Using '0.0.0.0' as host and '8000' as port, you can change it by editing 'resources/config.edn'")
    (apply tmpl/->files data
           (concat
             [["src/{{sanitized}}/components.clj" (render "src/components.clj" data)]]
             (when (:datalevin data)
               [["src/{{sanitized}}/db/datalevin/config.clj" (render "src/db/datalevin/config.clj" data)]])
             (when (:http-server data)
               [["src/{{sanitized}}/diplomat/http_server/hello_world.clj" (render "src/diplomat/http_server/hello_world.clj" data)]
                ["src/{{sanitized}}/diplomat/http_server.clj" (render "src/diplomat/http_server.clj" data)]])
             (when (:telegram data)
               [["src/{{sanitized}}/diplomat/telegram/consumer.clj" (render "src/diplomat/telegram/consumer.clj" data)]])
             [["resources/config.edn"             (render "resources/config.edn" data)]
              ["Dockerfile"                        (render "Dockerfile" data)]
              [".dockerignore"                     (render "dockerignore" data)]
              [".gitignore"                        (render "gitignore" data)]
              ["project.clj"                       (render "project.clj" data)]
              ["reflect-config.json"               (render "reflect-config.json" data)]
              ["README.md"                         (render "README.md" data)]
              [".clj-kondo/config.edn"             (render "clj-kondo/config.edn" data)]
              [".lsp/config.edn"                   (render "lsp/config.edn" data)]
              [".github/workflows/lint.yml"        (render "github/workflows/lint.yml" data)]
              [".github/workflows/tests.yml"       (render "github/workflows/tests.yml" data)]
              [".github/workflows/build.yml"       (render "github/workflows/build.yml" data)]]))))
