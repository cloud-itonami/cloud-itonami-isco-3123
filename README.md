# cloud-itonami-isco-3123

Open Occupation Blueprint for **ISCO-08 3123**: Construction Supervisors.

This repository designs a forkable OSS business for an independent construction site supervisor: a site-walkthrough robot performs safety-checklist inspection and progress documentation under a governor-gated actor, so the supervisor keeps their own inspection and safety records instead of renting a closed site-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a site-walkthrough robot performs safety-checklist inspection and progress-photo documentation under an actor that proposes
actions and an independent **Construction Supervision Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
operating near active equipment, or clearing a site for the next work phase) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
site plan + safety checklist + crew schedule
        |
        v
Site Supervision Advisor -> Construction Supervision Governor -> coordinate/clear, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `3123`). Required capabilities:

- :robotics
- :forms
- :telemetry
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
