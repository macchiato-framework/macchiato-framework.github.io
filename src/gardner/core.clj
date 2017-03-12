(ns gardner.core
  (:require [clojure.walk :as walk]
            [garden.core :as garden]
            [gardner.autoprefixer :as prefix]
            [garden.color :as c]
            [garden.units :as u]))

(def colors
  (atom {}))

(def vars
  (atom {:v 18
         :h 10
         :g 300}))

(def b
  {:dashed "dashed"
   :solid  "solid"})

(defn $padding
  ([x] ($padding x x x x))
  ([tb rl] ($padding tb rl tb rl))
  ([t rl b] ($padding t rl b rl))
  ([t r b l]
   (let [vars @vars
         v (:v vars)
         h (:h vars)]
     {:padding {:top    (when t (u/px* v t))
                :left   (when l (u/px* h l))
                :bottom (when b (u/px* v b))
                :right  (when r (u/px* h r))}})))

(defn $margin
  ([x] ($margin x x x x))
  ([tb rl] ($margin tb rl tb rl))
  ([t rl b] ($margin t rl b rl))
  ([t r b l]
   (let [vars @vars
         v (:v vars)
         h (:h vars)]
     {:margin {:top      (when t (u/px* v t))
               :left     (when l (u/px* h l))
               :bottom   (when b (u/px* v b))
               :right    (when r (u/px* h r))}})))

(defn &c [x]
  (or (get @colors x) (throw (Exception. (str "Do not know color " x " only " @colors)))))

(defn &v [x] (and x (u/px* (get @vars :v) x)))
(defn &h [x] (and x (u/px* (get @vars :h) x)))
(defn &b [x] (and x (get b x)))

(defn lighten
  "lighten hex -> hsl ->hex"
  [color amount]
  (c/hsl->hex (c/lighten (&c color) amount)))

(defn darken
  "darken hex -> hsl -> hex"
  [color amount]
  (c/hsl->hex (c/darken (&c color) amount)))

(defn $text
  ([s] {:font-size (&v s)})
  ([s l] {:font-size (&v s) :line-height (&v l)})
  ([s l & args]
   (let [props {:left :text-align
                :right :text-align
                :center :text-align
                :uppercase :text-transform
                :baseline :vertical-align
                :bold :font-weight
                :normal :font-weight}]
     (reduce (fn [acc k]
               (cond
                 (number? k) (assoc acc :font-weight k)
                 (get props k) (assoc acc (get props k) (name k))
                 :else (throw (Exception. (str "Do not know " k)))))
             {:font-size (&v s) :line-height (&v l)} args))))

(defn &border
  ([width color]
    {:width (u/px width)
     :style "solid"
     :color (&c color)})
  ([type width color]
    {:width (u/px width)
     :style (&b type)
     :color (&c color)}) )

(defn $border
  ([width color] {:border (&border width color)})
  ([type width color] {:border (&border type width color)})
  ([pos type width color] {:border {pos (&border type width color)}}) )

(defn $border-color
  ([color] {:border-color (&c color)}))


(defn $width ([w] {:width (&h w)}))
(defn $height ([h] {:height (&v h)}))
(defn $min-height ([h] {:min-height (&v h)}))
(defn $min-width ([w] {:min-width (&h w)}))
(defn $flex-basis ([w] {:flex-basis (&h w)}))
(defn $max-height ([h] {:max-height (&v h)}))
(defn $max-width ([w] {:max-width (&h w)}))


(defn $absolute [t r b l]
  {:position "absolute"
   :top (&v t)
   :left (&h l)
   :right (&h r)
   :bottom (&v r)})

(defn $fixed [t r b l]
  {:position "absolute"
   :top (&v t)
   :left (&h l)
   :right (&h r)
   :bottom (&v r)})

(defn $block [& xs]
  (merge {:display "block"} (apply $padding xs)))

(defn $color
  ([f] {:color (&c f)})
  ([f b] {:color (&c f) :background-color (&c b)}))

(defn $fill [f] {:fill (&c f)})


(defn $bg-color [c]
  {:background-color (&c c)})


(defn $push-top ([x] {:margin-top (&v x)}))
(defn $push-bottom ([x] {:margin-bottom (&v x)}))
(defn $push-left ([x] {:margin-left (&v x)}))
(defn $push-right ([x] {:margin-right (&v x)}))

(defn $clear  []
  {:border "none" 
   :outline "none"
   :background "none"} )

(def macros
  (atom
    {:$padding $padding
     :$margin  $margin
     :$text $text
     :&border &border
     :$clear $clear
     :$border $border
     :$border-color $border-color
     :$color $color
     :$block $block
     :$bg-color $bg-color
     :$push-top $push-top
     :$push-bottom $push-bottom
     :$push-right $push-right
     :$push-left $push-left
     :$absolute $absolute
     :$width $width
     :$fill $fill
     :$height $height
     :$min-height $min-height
     :$min-width $min-width
     :$max-height $max-height
     :$max-width  $max-width
     :$flex-basis  $flex-basis
     :&v &v
     :&h &h
     :&c &c
     :$fixed (fn [t r b l]
               {:position "fixed"
                :top    (when t (&v t))
                :right  (when r (&h r))
                :bottom (when b (&v b))
                :left   (when l (&h l)) })
     :$block-left (fn [w]
                    {:position "fixed"
                     :top 0 :bottom 0 :left 0
                     :width (&h w)})

     :$block-top (fn [h l r]
                   {:position "fixed"
                    :top 0
                    :left   (&h l)
                    :right  (&h r)
                    :height (&v h)  })

     :$circle (fn [] {:border-radius "50%" })

     :$flex-full (fn [_] {:flex-grow 1})

     :$flex-self-stretch (fn [] {:align-self "stretch"})


     :$flex-column (fn
                     ([] {:display ["flex" ]
                          :justify-content "space-between"
                          :flex-direction "column"
                          :align-items "baseline"})
                     ([a] {:display ["flex" ]
                           :justify-content "space-between"
                           :flex-direction "column"
                           :align-items a})
                     ([a j] {:display ["flex" ]
                             :justify-content j
                             :flex-direction "column"
                             :align-items a}))

     :$flex-row (fn
                  ([]
                   {:display "flex"
                    :justify-content "space-between"
                    :align-items "baseline"})
                  ([a]
                   {:display "flex"
                    :justify-content "space-between"
                    :align-items a})
                  ([a j]
                   {:display "flex"
                    :justify-content j
                    :align-items a})
                  ([a j ac]
                   {:display "flex"
                    :justify-content j
                    :align-items a
                    :align-content ac }))

     :$flex-row-left (fn []
                       {:display "flex"
                        :justify-content "flex-start"
                        :align-items "baseline"})

     :$center (fn []
                {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :flex-direction "column" } )

     :$center-block (fn
                      ([w] {:width (&h w)
                            :height (&h w)
                            :min-width (&h w)
                            :min-height (&h w)
                            :display "flex"
                            :align-items "center"
                            :justify-content "center"
                            :flex-direction "column" })
                      ([w h] {:width  (when w (&h w))
                              :height (when h (&v h))
                              :display "flex"
                              :align-items "center"
                              :justify-content "center"
                              :flex-direction "column" }))

:$box (fn
        ([w] {:display "block"
              :width  (&h w)
              :height (&h w) })

        ([w h] {:display "block"
                :width  (when-not (= w nil) (&h w))
                :height (when-not (= h nil) (&v h)) }) )

:$block-main (fn [t r b l]
               {:position "fixed"
                :top    (&v t)
                :right  (&h r)
                :bottom (&v b)
                :left   (&h l)})

:$block-bottom (fn [h l r]
                 {:position "fixed"
                  :bottom 0
                  :left   (&h l)
                  :right  (&h r)
                  :height (&v h)})
:$cursor (fn [] {:cursor "pointer"})

    }))

(defn prop-macro? [x]
  (and (keyword? x) (re-find #"^\$" (name x))))

(defn rule-macro? [x]
  (and (keyword? x) (re-find #"^\&" (name x))))

(defn pre-process-rule [[x & xs :as r]]
  (if-let [h (and (rule-macro? x) (get @macros x))]
    (apply h xs)
    r))

(defn pre-process-props [x]
  (let [macros @macros]
    (reduce (fn [acc [k v]]
              (if (prop-macro? k)
                (if-let [h (get macros k)]
                  (merge acc (if (vector? v)
                               (apply h v)
                               (h v)))
                  acc)
                (assoc acc k v)))
            {} x)))

(defn pre-process-constant [x]
  (or (get @vars x) x))

(defn pre-process [grdn]
  (walk/postwalk (fn [x]
                  (cond
                    (vector? x) (pre-process-rule x)
                    (map? x) (pre-process-props x)
                    (keyword? x) (pre-process-constant x)
                    :else x))
                grdn))

(defn css [grdn]
  (prefix/autoprefix
    (garden/css (pre-process grdn))))

(defn resolve-var [v] (if (var? v) (var-get v) v))

(defn config [{vs :vars cs :colors ms :macros :as opts}]
  (when vs (swap! vars merge (resolve-var vs)))
  (when cs (swap! colors merge (resolve-var cs)))
  (when ms (swap! macros merge (resolve-var ms))))

(comment 
  (css [:body {:$color [:red :blue]
               :$absolute [nil 1 2 0.5]
               :$margin [3]
               :$width [4]}]))
