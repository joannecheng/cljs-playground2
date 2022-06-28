(ns app.cards.js-interop-talk
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [devcards.core :as dc :refer [defcard]]
            [goog.object :as gobj]
            [goog.array :as garray]))

;; 
;;covering the similarities in syntax with Clojure/Java, basically pointing out the same things from what you have written up
;;pointing out some minor JS-specific differences (accessing properties in JS objects, for instance, converting from JS structures to CLJS structures and back)
;;Interop with native browser functions
;;Interop with external node_modules with shadow_cljs (with also a note on how a dev should look at the google closure library before depending on external node modules)
;;Promises

(defn app []
  [:div
   [:h1 "ClojureScript and JS Interop Talk"]
   [:div {:id "main"}
    "Some examples will go here"]])

(defcard js-interop
  (dc/reagent [app]))

(defcard js-functions
  "# Basic Interop")

(comment
  "Basic Interop Code examples"

  ;; Calling a function
  ;; document.getElementById ("main")
  (.getElementById js/document "main")

  ;; Getting a property
  ;; window.innerHeight
  (.-innerHeight js/window)

  ;; Getting nested properties
  ;; document.location.href
  (.-href (.-location js/document))
  (.. js/document -location -href)

  ;; Using JS objects in CLJS  
  ;; [3 1 2 4].length
  (.-length #js [3 1 2 4])
  ;; Notice the #js literal - this means that whatever is following it is a JS Obj or Array (in this case, array)
  ;; Can do the same for Objects
  (def a-new-js-obj #js {:user "joannecheng" :first-name "Joanne"})


  ;; There are also functions for translating JS objects into CLJS, and vice versa 
  ;; However, they're pretty time intensive functions. Use with caution:
  (js->clj #js {:first-name "Joanne" :address "333 Main Street"})
  (js->clj #js {:first-name "Joanne" :address "333 Main Street"} :keywordize-keys true)

  ;; But #js only affects top level of arrays and objects. Use clj->js for nested structures
  (js->clj {:user {:name "joanne" :state "VA"}})

  (clj->js [1 2 3 4 5])

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
  ;; Modifying 
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
  ;; If you find yourself needing to apply some changes to your JS objects/arrays,
  ;; there are some helpful functions

  ;; For arrays:
  (def arr #js [1 2 3 4 5])
  (aget arr 2)
  (aset arr 2 10000)
  arr
  ;; => #js [1 2 10000 4 5]

  ;; For objects: use goog.object/get and goog.object/set
  (def new-obj #js {:full-name "Joanne" :occupation "Software Developer"})
  (goog.object/get new-obj "full-name")
  (goog.object/set new-obj "full-name" "Joanne Cheng")
  new-obj

  ;; Notice we're not using the .- notation when we use `get`. 
  ;; What is this 'goog.object' thing? This is from the Google Closure Library/compiler.
  ;; Remember when we talked about externing above?


  ;; General Rules when accessing JS obj properties:
  ;; Use the `.-` notation when you access anything in the js namespace or with externs
  ;; (example: window.location.href)
  ;; Use google closure getters (`gobj/get`, `gobj/getValueInKeys`) when dealing with any
  ;; dynamic JS objects (eg: a JSON response from an endpoint



  ;; These are handy when you use a third party JS library and need to send it data
  ;; Speaking of third party JS libraries, how do you get them in your project?



  ;; Promises (and putting this all together)
  )

(defcard google-closure-compiler
  "# Google Closure compiler

* Do you _really_ need that npm dependency?
* example: Date localization vs moment js
  * Not exactly the easiest to work with, but removes the need for an external dependency
 in your project
  * Goog.uri https://google.github.io/closure-library/api/goog.Uri.html
  * positioning, math functions: https://google.github.io/closure-library/api/goog.math.html
  * And much more!
 ")

(defcard accessing-js-objects
  "# Accessing JS Objects

  * js->clj / clj->js (time intensive)
  * .- notation ( or (.. jsobj -prop1 -prop2))
  * goog.object/get (get) | goog.object/getValueByKeys  (get-in)
  * Advanced compilation notes: often times when we're dealing with 
  js objects in clojurescript, we're getting data from elsewhere using JS, like using
  the fetch API and parsing JSON into a JS object 

   There is a compilation setting in the Google Closure compiler called 'advanced compilation'
   that's invoked through your clojurescript compiler. This setting will rename the property names
   in your JS object. Ex, you get a JSON object, and ':user-name' in {'user-name': 'Joanne'} can be renamed to any characters.
   
   To prevent this from happening, I prefer using `gobj/get` when accessing js objects
   that are not defined in your code.

   There are also issues when you need to get data in nested DS,
   you can use `gobj/getValueInKeys` which acts like `clojure.core/get-in`

   Use the `.-` notation when you access anything with externs (example: window.location.href)
   Use google closure getters (`gobj/get`, `gobj/getValueInKeys`) when dealing with any
   dynamic JS objects (eg: a JSON response from an endpoint)")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Promises
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defcard promises-text
  "# Promises

  There are several ways to work with JS promises in ClojureScript

  ## Directly calling js/Promise
  * refernce: [Promises in ClojureScript](https://gist.github.com/pesterhazy/c4bab748214d2d59883e05339ce22a0f )

  ## Async/Await
  * [Using Promesa](https://github.com/funcool/promesa)")


(def !resp (atom nil))
(defn k->f [k]
  (-> (- k 273.15)
      (* 1.8)
      (+ 32)))

(defn promise-example+
  "Getting the temperature for Richmond, VA"
  []
  (let [url (str "https://api.openweathermap.org/data/2.5/weather?lat="
                 37.5407
                 "&lon="
                 -77.4360
                 "&appid="
                 "2b4703d8df2665d833055a65aef9e49a")]
    (-> (js/fetch url)
        (.then #(.json %))
        (.then #(reset! !resp %))
        (.catch #(prn "ERROR")))))
(promise-example+)
@!resp

(-> @!resp
    (gobj/getValueByKeys #js ["main" "temp"])
    k->f)

(defn weather-url [lat lng]
  (str "https://api.openweathermap.org/data/2.5/weather?lat="
       lat
       "&lon="
       lng
       "&appid="
       "2b4703d8df2665d833055a65aef9e49a"))

(def !richmond-resp (atom nil))
(def !abq-resp (atom nil))
(defn promise-all-example+
  "Getting the weather for Richmond, VA and Albuquerque, NM"
  []
  (let [richmond (weather-url 37.5407 -77.4360)
        abq (weather-url 35.0844 106.6504)]
    (prn richmond abq)
    (-> (js/Promise.all #js [(-> (js/fetch richmond) (.then #(.json %)))
                             (-> (js/fetch abq) (.then #(.json %)))])
        (.then (fn [[richmond-resp abq-resp]]
                 (reset! !richmond-resp richmond-resp)
                 (reset! !abq-resp abq-resp))))))

(promise-all-example+)
(-> @!richmond-resp
    (gobj/getValueByKeys #js ["weather" 0 "description"]))
(-> @!abq-resp
    (gobj/getValueByKeys #js ["weather" 0 "description"]))

;; Note: if you need async/await-like syntax, you can look into kitchen-async or core.async
;; I am not familiar with those ways of working, using native browser Promises works well for me
;; My opinion: js/Promise is robust, supported on all browsers, works for almost all cases.


(defcard interesting-resources
  "The Next Five Years of Cljs: https://www.youtube.com/watch?v=mty0RwkPmE8
   ClojureScript Compiler: Look Behind the Curtains https://www.youtube.com/watch?v=Elg17s_nwDg 
   http://app.klipse.tech/
   Type Inference in CLJS: https://blog.klipse.tech/clojure/2019/05/20/type-inference-in-clojurescript.html (tldr: we type hint because it helps our JS code run faster because our code has to do less guessing about what we're passing around to functions)
   https://www.juxt.pro/blog/clojurescript-app-performance
   David Nolen - (talking about recent developments in extern inference)[https://www.youtube.com/watch?v=CwpV_TNn-3s&t=781s]
   Promises in Cljs: https://gist.github.com/pesterhazy/c4bab748214d2d59883e05339ce22a0f
   ")