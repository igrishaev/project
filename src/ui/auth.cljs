(ns ui.auth
  (:require [ui.common :refer (js-stub)]

            [clojure.string :as str]

            [ajax.core :as ajax]
            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [re-frame.core :as rf]))


(rf/reg-sub
 :auth/user
 (fn [db [_ ]]
   (get db :user)))


(rf/reg-event-fx
 :auth/logout
 (fn [_ [_]]
   {:dispatch [:ui.events/api.call :logout
               nil
               [:auth/logout.ok]]}))


(rf/reg-event-fx
 :auth/logout.ok
 (fn [{db :db} [_]]
   {:db (dissoc db :user)
    :dispatch [:bar/info "See you again!"]}))


(rf/reg-event-fx
 :auth/email-init
 (fn [_ [_ email]]
   {:http-xhrio {:method :post
                 :uri "/auth/email/init"
                 :format (ajax/json-request-format)
                 :params {:email email}
                 :response-format (ajax/json-response-format
                                   {:keywords? true})
                 :on-success [:auth/email-init.ok]
                 :on-failure [:auth/email-init.err]}}))


(rf/reg-event-fx
 :auth/email-init.ok
 (fn [db [_ data]]
   (let [{:keys [message]} data]
     {:dispatch [:bar/info message]})))


(rf/reg-event-fx
 :auth/email-init.err
 (fn [_ [_ data]]
   ;; TODO connection error case
   ;; (js/console.log (clj->js data))
   (let [message (some-> data :response :error-message)]
     {:dispatch [:bar/error message]})))


(def form-auth
  [:input
   {:placeholder "Email"
    :type "text"
    :field :text
    :id :email}])


(defn view-auth-form
  []
  (let [doc (r/atom {})

        handler
        (fn [e]
          (.preventDefault e)
          (let [{:keys [email]} @doc]
            (rf/dispatch [:auth/email-init email])))]

    (fn []
      [:div#auth-form
       [:h3 "Sign in by email"]
       [:p.subtitle
        "No password is required. We'll send you a secret link."]
       [:div
        [:form {:on-submit handler}
         [bind-fields form-auth doc]
         [:button.btn-submit
          {:type "Button"
           :on-click handler}
          "Send"]]]
       [:h3 "or"]
       ;; TODO init Google with POST
       [:a.action-main
        {:href "/auth/google/init"}
        "Stay with your Google account"]])))


(defn view-auth
  []
  (if-let [user @(rf/subscribe [:auth/user])]
    [:div "user"]
    [view-auth-form]))


(defn view-user-block
  []
  (when-let [user @(rf/subscribe [:auth/user])]

    (let [{:keys [email
                  source
                  avatar_url
                  name]} user

          name (or name email)
          avatar_url (or avatar_url "/img/account.png")]

      [:div.dropdown.menu-item.last
       [:img.avatar
        {:src avatar_url}]
       [:div.dropdown-content
        [:span.header name
         [:br]
         [:small "Signed via " (str/capitalize source)]]
        [:a {:href js-stub
             :on-click #(rf/dispatch [:auth/logout])}
         "Logout"]]])))
