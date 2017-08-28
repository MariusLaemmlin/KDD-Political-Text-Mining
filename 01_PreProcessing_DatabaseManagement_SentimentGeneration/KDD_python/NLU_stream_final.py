import NLU_API as nlu
import json
from pprint import pprint
import pandas as pd
from watson_developer_cloud import WatsonException
from watson_developer_cloud import watson_developer_cloud_service
from pymongo import MongoClient
import time


text = open('news1_15_big7.txt', 'r')

df_news = pd.read_table(text, sep = ';', error_bad_lines=False, header = None)

#News
text_file = df_news[3][1:]
id_file = df_news[1][1:]

print(text_file)

######################
# USER Liste
####################

user_liste = ['USERNAME']
pw_liste = ['PASSWORD']

count = 1
count_user = 12

# Input: erste Werte aus der Userliste
user1 = user_liste[count_user]
pw1 = pw_liste[count_user]

#Texte versenden zu Watson
for text in text_file:
    try:

           #Die Funktion wird aufgerufen
            nlu_json = nlu.nlu_api(text,user1,pw1)
            nlu_json_data = json.loads(nlu_json)
            nlu_json_data['id'] = str(id_file[count])
            count += 1
            print (str(count) + str(nlu_json_data))
            enlu = str(nlu_json_data)
            #Rueckgabe jsonfiles von Watson
            with open('general_sentiments_id_newsbig71_15.json', 'a') as outfile:
                json.dump(nlu_json_data, outfile, indent=2)
                #json.dump(enlu, outfile, indent=2)

    #Errorcatching
    except WatsonException:
        print("Userwechsel " + str(count_user))
        count_user += 1
        count += 1
        user1 = user_liste[count_user]
        pw1 = pw_liste[count_user]
        #time.sleep(120)
        if count_user + 1 == len(user_liste):
            print("Userwechsel auf Null " + str(count_user))
            count_user = 0
            user1 = user_liste[count_user]
            pw1 = pw_liste[count_user]
            #time.sleep(360)
        pass
