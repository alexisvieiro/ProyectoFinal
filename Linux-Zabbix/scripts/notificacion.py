#! /usr/bin/python3
# Send to single device.
from pyfcm import FCMNotification
import sys

n = len(sys.argv)
print("Total arguments passed:", n)
 
push_service = FCMNotification(api_key="AAAAzpSqJsk:APA91bHed_OxPPS9q3_qyUL6qsKZjtBOWZhDfdQia_l36GO0WB2H29gAkRX7pf8Xi1mAEEA0xlJ2Xvf6T7LrGnnC7e0Snk6Mm5MkmKcf0as9D2jH2decOBwlWGTm2KInMokRc6HeYCG2")

# Your api-key can be gotten from:  https://console.firebase.google.com/project/<project-name>/settings/cloudmessaging
#ejlw1w0pQeulBTUbZZTil4:APA91bHOUp7O-vTtklHiCA_ZMTjD7gBCgJ1VTAVzNWrMgH8FbMfp3MeTqCQ-zsTxpV3lGox0CVKjVVDxbtY143wh8XMWCvIhR8f5-WkfbX-2MgXb50pKZX1RsMlIbU2qMShsXDUkZAOo
registration_id = sys.argv[1]
message_title = sys.argv[2]
message_body = sys.argv[3]
result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)

print(result)
