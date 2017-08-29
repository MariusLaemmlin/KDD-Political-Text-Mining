import bs4

from urllib.request import urlopen as uReq
from urllib.request import Request, urlopen
import urllib
from bs4 import BeautifulSoup as soup
import ssl
import pandas as pd
import numpy as np




ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

#Input: text fils  with urls for the 7 newspapers, for everyday within the time frame from 1st to 20th of june
df_news = pd.read_table('reuters_input.txt', sep = '\t', error_bad_lines=False, header = None)


for i in range(len(df_news[2])):
    try:

        req_client = uReq(df_news[4][i],context=ctx)
        page_html = req_client.read()
        #print(page_html)
        req_client.close()

        page_soup = soup(page_html, 'html.parser')

        #exclude html parts that we do not need
        for match in page_soup.findAll('span'):
            match.unwrap()
        for match in page_soup.findAll('a'):
            match.unwrap()
        for match in page_soup.findAll('img'):
            match.unwrap()
        #extract text parts
        text_only = str(page_soup.findAll('p'))
        t = []
        for p_tag in page_soup.findAll('p'):
             te = p_tag.text
             t.append(te)

        ts = str(t)
        new = ts.replace(',','  ')
        new2 = new.replace('<p>', ' ')
        new3 = new2.replace('</p>', ' ')
        new4 = new3.replace('\n' , ' ')
        new5 = new4.replace('"','' )
        new6 = new5.replace("'", '')
        new7 = new6.replace('[', '')
        new8 = new7.replace(']', '')
        new9 = new8.replace('\t', '')
        new10 = new9.replace('\n', '')


        with open('big7_16_30.txt', 'a') as outfile:

            outfile.write('\n')
            outfile.write(str(df_news[2][i]) + ',')
            outfile.write(str(df_news[1][i]) + ',')
            outfile.write(str(df_news[0][i]) + ',')
            outfile.write(new8)
            outfile.write('\n')
    except:
        pass



