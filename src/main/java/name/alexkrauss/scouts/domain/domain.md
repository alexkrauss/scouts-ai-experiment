# Domain of the Scouts application.

Scouts is a web application for managing data for a scout organization. Its main
purpose is to track registration of scouts for events. It aims to make the process
digital, to avoid repetitive paperwork.


## Overview

* The organization consists of several *groups*, who participate in events.
* *Scouts* are the individuals that form a group.
* *Events* are happenings for which the individuals must register. Events are often 
  attended by multiple groups, but not all groups always participate in all events.
* *Registrations* are the act of a scout registering for an event. They are created by scouts
  themselves or rather their parents.
* *Accounts* are used to log into the system. They usually belong to the parents of the scouts,
  as they are the ones registering the scouts for events.


## Entities

### Scout

Scouts have 

* a name,
* a birthdate
* an address
* a phone number (optional)
* the name of the health insurance
* allergy information
* vaccination information

Furthermore, they have a list of contacts, which are usually the parents of the scout.
A contact is a name, a phone number, an email adress and a field describing the relationship,
e.g. (mother, father, guardian).

There is also a field indicating when the data was last updated or checked.

### Group

Groups just have a name.

### Event

Events have

* a name,
* a start and end date,
* a meeting point
* a location
* a list of groups that can participate in the event.
* a cost for participation.

Further, there is a text for additional information.

### Registration

For the registration, we must know

* The scout
* The event

* A note, which is a free text field for additional information.

* The status of the registration, which can be one of the following:
  * *Pending*: The registration is not yet confirmed.
  * *Confirmed*: The registration is confirmed.
  * *Cancelled*: The registration is cancelled.

* The date of the registration.
* The account that created the registration.

### Account

Accounts have a name, an email address and login credentials. They are used to log into the system.

