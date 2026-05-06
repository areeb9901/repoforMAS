Clarification on Beagle Monitor Page Checks


Hi [BA Name],

I checked the Beagle Monitor Page implementation for the below checks:

Checking Connection to Negotiation Document Server

Checking Connection to Feed

Please find the details below.

For the “Checking Connection to Negotiation Document Server” check, Beagle is not actually connecting to a separate external document server/API.This check validates whether the application has proper read/write access to the file system location where negotiation document attachments are stored.

Technically, the monitor invokes a file system access check on the attachments directory configured under Beagle’s data directory (attachments folder).

For the “Checking Connection to Feed” check, the monitor validates whether the main JMS/MQ feed connection configured for the feedService is active.

The check confirms whether the MQ connection object has been successfully established for the configured inward/outward queues.It does not validate all Beagle feeds individually. For example, separate feed services such as the MAOS feed are configured independently and are not specifically checked by this monitor validation.

Please let me know if you need any additional technical details.

Best regards,Areeb




The “Negotiation Document Server” check verifies whether Beagle has read/write access to the file system location where negotiation document attachments are stored. It is effectively checking the document attachment storage path, not calling a separate external server.


“Negotiation Document Server” is actually a file-system access check for the Beagle attachments directory used to store negotiation documents.
“Connection to Feed” checks only the main JMS/MQ feedService connection, based on whether its MQ connection object is active. It does not validate each individual feed separately.