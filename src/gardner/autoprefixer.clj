(ns gardner.autoprefixer
  (:require [clojure.string :as s]))

(def autoprefixes
  [["display: flex;"

    "display: -webkit-box;
    display: -webkit-flex;
    display: -ms-flexbox;
    display: flex;"]

   ;; flex-direction
   ["flex-direction: column;"

    "-webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    -webkit-flex-direction: column;
    -ms-flex-direction: column;
    flex-direction: column; " ]

   ["flex-direction: column-reverse;"

    "-webkit-box-orient: vertical;
    -webkit-box-direction: reverse;
    -webkit-flex-direction: column-reverse;
    -ms-flex-direction: column-reverse;
    flex-direction: column-reverse; " ]

   ["flex-direction: row;"

    "-webkit-box-orient: horizontal;
    -webkit-box-direction: normal;
    -webkit-flex-direction: row;
    -ms-flex-direction: row;
    flex-direction: row; " ]

   ["flex-direction: row-reverse;"

    "-webkit-box-orient: horizontal;
    -webkit-box-direction: reverse;
    -webkit-flex-direction: row-reverse;
    -ms-flex-direction: row-reverse;
    flex-direction: row-reverse; " ]

   ;; justify-content
   ["justify-content: center;"

    "-webkit-box-pack: center;
    -webkit-justify-content: center;
    -ms-flex-pack: center;
    justify-content: center;" ]

   ["justify-content: flex-start;"

    "-webkit-box-pack: start;
    -webkit-justify-content: flex-start;
    -ms-flex-pack: start;
    justify-content: flex-start;" ]

   ["justify-content: flex-end;"

    "-webkit-box-pack: end;
    -webkit-justify-content: flex-end;
    -ms-flex-pack: end;
    justify-content: flex-end;" ]

   ["justify-content: space-around;"

    "-webkit-justify-content: space-around;
    -ms-flex-pack: distribute;
    justify-content: space-around;" ]

   ["justify-content: space-between;"

    "-webkit-justify-content: space-between;
    -ms-flex-pack: justify;
    justify-content: space-between;" ]

   ;; align-items
   ["align-items: center;"

    "-webkit-box-align: center;
    -webkit-align-items: center;
    -ms-flex-align: center;
    align-items: center;" ]

   ["align-items: flex-start;"

    "-webkit-box-align: start;
    -webkit-align-items: flex-start;
    -ms-flex-align: start;
    align-items: flex-start;" ]

   ["align-items: flex-end;"

    "-webkit-box-align: end;
    -webkit-align-items: flex-end;
    -ms-flex-align: end;
    align-items: flex-end;" ]

   ["align-items: stretch;"

    "-webkit-box-align: stretch;
    -webkit-align-items: stretch;
    -ms-flex-align: stretch;
    align-items: stretch;" ]

   ["align-items: baseline;"

    "-webkit-box-align: baseline;
    -webkit-align-items: baseline;
    -ms-flex-align: baseline;
    align-items: baseline;" ]

   ;; align-self
   ["align-self: center;"

    "-webkit-align-self: center;
     -ms-flex-item-align: center;
      align-self: center;" ]

   ["align-self: flex-start;"

    "-webkit-align-self: flex-start;
     -ms-flex-item-align: start;
      align-self: flex-start;" ]

   ["align-self: flex-end;"

    "-webkit-align-self: flex-end;
     -ms-flex-item-align: end;
      align-self: flex-end;" ]

   ["align-self: stretch;"

    "-webkit-align-self: stretch;
     -ms-flex-item-align: stretch;
      align-self: stretch;" ]

   ["align-self: baseline;"

    "-webkit-align-self: baseline;
     -ms-flex-item-align: baseline;
      align-self: baseline;" ]

   ["align-self: auto;"

    "-webkit-align-self: auto;
     -ms-flex-item-align: auto;
      align-self: auto;" ]

   ;; flex-wrap
   ["flex-wrap: wrap;"

    "-webkit-flex-wrap: wrap;
     -ms-flex-wrap: wrap;
         flex-wrap: wrap;" ]

   ["flex-wrap: wrap-reverse;"

    "-webkit-flex-wrap: wrap-reverse;
     -ms-flex-wrap: wrap-reverse;
         flex-wrap: wrap-reverse;" ]

   ["flex-wrap: nowrap;"

    "-webkit-flex-wrap: nowrap;
     -ms-flex-wrap: nowrap;
         flex-wrap: nowrap;" ]

   ;; flex-grow
   ["flex-grow: 1;"
    "-webkit-box-flex: 1;
     -webkit-flex-grow: 1;
         -ms-flex-positive: 1;
             flex-grow: 1;" ]

   ["flex-grow: 0;"
    "-webkit-box-flex: 0;
     -webkit-flex-grow: 0;
         -ms-flex-positive: 0;
             flex-grow: 0;" ]

   ]
  )

(defn autoprefix [st]
  (reduce (fn [acc v]
            (s/replace acc (v 0) (v 1))
            ) st autoprefixes))
