(ns construction-supervision.governor
  "ConstructionSupervisionGovernor — the independent safety/
  traceability layer for the ISCO-08 3123 independent construction-
  supervision actor. Wired as its own `:govern` node in
  `construction-supervision.actor`'s StateGraph, downstream of
  `:advise` — the Advisor has no notion of site provenance or active-
  equipment/phase-clearance risk, so this MUST be a separate system
  able to reject a proposal (itonami actor pattern, per
  ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. site provenance  — the request's site must be registered.
    2. no-actuation       — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: operating near active equipment and
  clearing a site for the next work phase always require human
  sign-off):
    3. :op :operate-near-active-equipment.
    4. :op :clear-site-for-next-phase.
    5. low confidence (< `confidence-floor`)."
  (:require [construction-supervision.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:operate-near-active-equipment :clear-site-for-next-phase})

(defn- hard-violations [{:keys [proposal]} site-record]
  (cond-> []
    (nil? site-record)
    (conj {:rule :no-site :detail "未登録 site"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `construction-supervision.store/Store`.
  Returns `{:ok? bool :violations [...] :confidence n :hard? bool
  :escalate? bool}`."
  [request context proposal store]
  (let [site-record (store/site store (:site-id request))
        hard (hard-violations {:proposal proposal} site-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
