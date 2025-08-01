#!/usr/bin/env bb

(require '[selmer.parser :as p])
(require '[clojure.java.io :as io])
(require '[clojure.string :as str])
(require '[clojure.edn :as edn])

(def versions (edn/read-string (slurp "script/versions.edn")))

(def version (str/trim (slurp (io/file "resources/CLJ_KONDO_VERSION"))))
(def stable-version (str/trim (slurp (io/file "resources/CLJ_KONDO_RELEASED_VERSION"))))

(def deps-edn (edn/read-string (slurp "deps.edn")))

(def deps-dependencies (:deps deps-edn))
(def lein-dependencies (cons '[org.clojure/clojure "1.10.3"]
                             (map (fn [[k v]]
                                    [k (:mvn/version v)])
                                  deps-dependencies)))

(def deps-test-dependencies (-> deps-edn :aliases :test :extra-deps))
(def lein-test-dependencies (keep (fn [[k v]]
                                    (when-not (#{'cognitect/test-runner 'org.clojure/clojure} k)
                                      [k (:mvn/version v)]))
                                  deps-test-dependencies))

(spit "project.clj" (str ";; GENERATED by script/update-project.clj, DO NOT EDIT\n"
                         ";; To change dependencies, update deps.edn and run script/update-project.clj.\n"
                         ";; To change other things, edit project.template.clj and run script/update-project.clj.\n"
                         "\n"
                         (p/render (slurp (io/file "project.clj.template"))
                                   (merge
                                    versions
                                    {:version version
                                     :dependencies
                                     (format "[%s]"
                                             (str/join "\n                 "
                                                       lein-dependencies))
                                     :test-dependencies
                                     (format "[%s]"
                                             (str/join "\n                                   "
                                                       lein-test-dependencies))}))))

(spit
 ".pre-commit-hooks.yaml"
 (str
  "# GENERATED by script/update-project.clj, DO NOT EDIT\n"
  "# To change hooks, edit .pre-commit-hooks.yaml.template and run script/update-templates.clj.\n"
  "\n"
  (p/render (slurp (io/file ".pre-commit-hooks.yaml.template")) {:version stable-version})))

(spit "script/extract-versions"
      (p/render (slurp (io/file "script/extract-versions.template"))
                versions))

;; [[org.clojure/clojurescript "1.10.520"] ;; for extraction tests
;;  [clj-commons/conch "0.9.2"]
;;  [jonase/eastwood "0.3.6"]
;;  [borkdude/missing.test.assertions "0.0.1"]]
