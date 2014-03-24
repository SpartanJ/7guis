; Adapted from http://blog.reactiveprogramming.org/?p=21

(ns cljguis.bookflight
  (:use [seesaw core]
    cljguis.core)
  (:require [seesaw.bind :as b]))

(defn date->str [date]
  (.format (java.text.SimpleDateFormat. "dd.MM.yyyy") date))

(defn str->date [str]
  (let [str (.trim str)
        date-format (java.text.SimpleDateFormat. "dd.MM.yyyy") 
        date (.parse date-format str)
        date-str (.format date-format date)]
    (when-not (= date-str str) (throw (java.text.ParseException. "Incorrect Date" 0)))
    date))

(defn date-str? [str]
  (try (str->date str) true
    (catch Exception e false)))

(defn bookflight-panel []
  (let [flight-type    (combobox :model ["one-way flight" "return flight"])
        start-date     (text)
        return-date    (text)
        book           (button :text "Book")
        check-date-str (fn [d] 
                         (b/bind d 
                           (b/transform #(if (date-str? %) "white" "lightcoral"))
                           (b/property d :background)))]
    ; Regarding the color of the text fields.
    (check-date-str start-date)
    (check-date-str return-date)
    ; Regarding the status of the return date text field.
    (b/bind
      flight-type
      (b/transform = "return flight")
      (b/property return-date :enabled?))
    ; Regarding the status of the book button.
    (b/bind
      (b/funnel flight-type start-date return-date)
      (b/transform (fn [[flight-type start-date return-date]] 
        (cond (= flight-type "one-way flight")  
                (date-str? start-date) 
              (= flight-type "return flight")
                (and (date-str? start-date) 
                  (date-str? return-date)
                  (>= 0 (compare  
                    (str->date start-date)  
                    (str->date return-date)))))))
      (b/property book :enabled?))
    (listen book :action (fn [_]
      (if (= "one-way flight" (selection flight-type))
        (alert (str "You have booked a one-way flight on " (value start-date)))
        (alert (str "You have booked a return flight on " (value start-date) " and " (value return-date))))))
    ; "Initialize" the bindings.
    (config! [start-date return-date] :text (date->str (java.util.Date.)))
    (selection! flight-type "one-way flight") 
    
    (grid-panel
      :rows 4
      :items [flight-type start-date return-date book]
      )))

(defn -main [& args] 
  (invoke-later
    (-> (frame :title "Book Flight" :content (bookflight-panel) :on-close :exit) 
      pack!
      show!)))
