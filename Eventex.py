from flask import Flask,render_template,url_for,request,make_response
import pandas as pd 
import pickle
from joblib import dump,load
import spacy
import re
import string
import datetime
import dateutil.parser
import base64
from bs4 import BeautifulSoup

app = Flask(__name__)
# @app.route('/')
# def home():
# 	return render_template('home.html')

@app.route('/predict',methods=['POST'])
def predict():
	try:
		if request.method == 'POST':
			msg = request.get_json()
			for i in msg['payload']['headers']:
				if(i['name']=='Subject'):
					subject = i['value']
					pattern = r'(?:Fwd:\s)'
					fwd1 = re.findall(pattern,subject,0)
					for i in fwd1:
						subject = subject.replace(i,"")
			symbols = ["[","]","(",")","{","}"]
			for i in symbols:
				subject = subject.replace(i,"")
			subject = subject.capitalize()
			print(subject)
							
			if(msg['payload']["mimeType"]=="multipart/alternative"):
				try:
					x = msg['payload']['parts'][0]["body"]["data"]
					convert = base64.urlsafe_b64decode(x.encode('utf-8'))
				except:
					return make_response({"prediction" : 0})
			elif(msg['payload']["mimeType"]=="multipart/mixed"):
				try:
					x = msg['payload']['parts'][0]['parts'][0]["body"]["data"]
					convert = base64.urlsafe_b64decode(x.encode('utf-8'))
				except:
					try:
						x = msg['payload']['parts'][0]["body"]["data"]
						convert = base64.urlsafe_b64decode(x.encode('utf-8'))
					except:
						return make_response({"prediction" : 0})
			elif(msg['payload']["mimeType"]=="multipart/related"):
				try:
					x = msg['payload']['parts'][0]['parts'][0]["body"]["data"]
					convert = base64.urlsafe_b64decode(x.encode('utf-8'))
				except:
					return make_response({"prediction" : 0})
			else:
				try:
					x = msg['payload']['body']['data']
					convert = base64.urlsafe_b64decode(x.encode('utf-8'))
				except:
					return make_response({"prediction" : 0})
		
		
			soup = BeautifulSoup(convert,"html5lib")
			body = soup.get_text()
			real_body=" ".join(body.split())
			regex = r'((?:---------- Forwarded message ---------)(?:.|\n)*(?:To:))'
			fwd2 = re.findall(regex,real_body,0)
			for i in fwd2:
				real_body=real_body.replace(i,"")
			real_body = subject+" "+real_body
			real_body = real_body.replace("*","")

			classifier = load('svm_model.joblib')
			cv = load('cv.joblib')

			data = [real_body]
			result = classifier.predict(cv.transform(data))
			if(result == 1):
				if (extract(real_body)==0):
					return make_response({"prediction" : 0})
				else:
					final_date = extract(real_body)[0]
					final_time = extract(real_body)[1]
					final_venue = extract(real_body)[2]
					final_link = extract(real_body)[3]
					return make_response({"prediction" : 1, "subject" : subject,"date" : final_date, "time" : final_time, "venue" : final_venue, "link" : final_link})
			else:
				return make_response({"prediction" : 0})
	except:
		return make_response({"prediction" : 0})
def extract(test_text):
	output_dir = 'C:/Users/user/Desktop/AppEventex/spacy_NER'
	nlp2 = spacy.load(output_dir)

	patdate = r'\b(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])\s+(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)(?:\s|-|to|and|&)+(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)(?:(?:,|\s)*(?:[1-2]\d{3}))?\b|\b(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])\s+(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:\s|-|to|and|&)+(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:(?:,|\s)*(?:[1-2]\d{3}))?\b|\b(?:(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)(?:,|\s)*)+(?:and|\s)*(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)(?:\s|of)*(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:,|\s)*(?:[1-2]\d{3})?\b|\b(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:\s|,)+(?:0[1-9]|1[0-9]|2[0-9]|3[0-1]|[1-9])(?:st|rd|th|nd)(?:(?:\s|,)*(?:[1-2]\d{3}))?\b|\b(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:\s|,)+(?:0[1-9]|1[0-9]|2[0-9]|3[0-1]|[1-9])(?:(?:\s|,|\')*(?:[1-2]\d{3}))?\b|\b(?:(?:[1-9]|0[1-9]|1[0-9]|2[0-9]|3[0-1])|(?:[1-9]|0[1-9]|1[0-9]|2[0-9]|3[0-1])(?:th|rd|st|nd))\s*(?:to|-|and|&)\s*(?:(?:[1-9]|0[1-9]|1[0-9]|2[0-9]|3[0-1])|(?:[1-9]|0[1-9]|1[0-9]|2[0-9]|3[0-1])(?:th|rd|st|nd))\s+(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:(?:\s|,)*(?:[1-2]\d{3}))?\b|\b(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)?(?:\.|\/|\-)(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:\.|\/|\-)?\s?(?:[1-2]\d{3}|1[0-9]|2[0-9])?\b|\b(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:\.|\/|\-)(?:0[1-9]|[1-9]|1[0-2])(?:\.|\/|\-)(?:[1-2]\d{3}|\d{2})\b|\b[1-2]\d{3}(?:\.|\/|\-)(?:0[1-9]|[1-9]|1[0-2])(?:\.|\/|\-)(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])\b|\b(?:(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])|(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|th|nd|rd))(?:\s|of)*(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:(?:,|\s|\')*(?:[1-2]\d{3}))?\b|\b(?:[Mm]onday|[Tt]uesday|[Ww]ednesday|[Tt]hursday|[Ff]riday|[Ss]aturday|[Ss]unday|[Mm]on|[Tt]ue|[Ww]ed|[Tt]hur|[Ff]ri|[Ss]at|[Ss]un|TODAY|TOMORROW|today|tomorrow|Today|Tomorrow)\b|\b(?:this|following|coming|next|forthcoming)\s(?:[Mm]onday|[Tt]uesday|[Ww]ednesday|[Tt]hursday|[Ff]riday|[Ss]aturday|[Ss]unday|[Mm]on|[Tt]ue|[Ww]ed|[Tt]hur|[Ff]ri|[Ss]at|[Ss]un|weekend|week)\b'
	patlink = re.compile('(?:https|http|www)\s+')
	pattime = re.compile('(?:0[1-9]|[1-9]|1[0-2])\s?(?:am|a\.m(?:\.)?|Am|AM|A\.M(?:\.)?|pm|p\.m(?:\.)?|Pm|PM|P\.M(?:\.)?)|(?:0[1-9]|[1-9]|1[0-9]|2[0-3])\s?(?:hrs|Hrs|hr|Hr)|(?:0[1-9]|[1-9]|1[0-9]|2[0-3])(?::|\.|\-)(?:0[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])\s*(?:hrs|Hrs|hr|Hr|am|a\.m(?:\.)?|Am|AM|A\.M(?:\.)?|pm|p\.m(?:\.)?|Pm|PM|P\.M(?:\.)?)|(?:12|12\.00|12:00)\snoon|(?:[1-9]|0[0-9]|1[0-9]|2[0-3])(?::|\.)(?:0[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])(?:\sIST)?')

	global position
	global arr
	global ven_pos
	global ven
	global time_pos
	global tim
	global cont_ven_pos
	global cont_ven
	global combven_pos
	global combven
	global link_pos
	global link_list
	global min_pos_arr
	global doc2

	
	min_pos_arr=[]
	doc2 = nlp2(test_text)
	position=[]                             #date positions (validated)
	arr=[]                                  #date validated
	ven_pos=[]                              #b-venue positions
	ven=[]                                  #b-venue text
	time_pos=[]                             #b-time positions (validated)
	tim=[]                                  #time validated and uni-formatted
	cont_ven_pos=[]                         #i-venue positions
	cont_ven=[]                             #i-venue text
	combven_pos=[]                          #b and i venue positions
	combven=[]                              #b and i venue text
	link_pos=[]                             #b-link positions
	link_list=[]                            #link text

	x=0                                     #iteration value for identified entities
	for ent in doc2.ents:                          #printing each entity and its label
		if ent.label_ == "B-DATE":                                  #date entities
			dat = ent.text
			if bool(re.fullmatch(patdate, dat)):                #validation with grammar patdate
				if dat.find(" to ") > 0:                            #replacing 'to' and '&' with '-'
					dat = dat.replace(" to ", "-")
				elif dat.find("&") > 0:
					dat = dat.replace("&", "-")
				position.append(x)
				arr.append(dat)
			else:
				continue
		elif ent.label_== "B-VENUE":
			if sum(c.isalpha() for c in ent.text)>0:                                                #b-venue entities
				ven_pos.append(x)
				ven.append(ent.text)
		elif ent.label_== "B-TIME" and bool(re.search(pattime,ent.text)):           #time entities and validation with grammar pattime
			time=ent.text
			dig = sum(c.isdigit() for c in time)                                    #dig stores number of digits in a time entity
			if dig == 1:
				if time.find("a") >= 0 or time.find("h") >=0 or time.find("A") >= 0 or time.find("H") >=0:
					time="0"+time[0]+':00 hrs'
				elif time.find("p") >= 0 or time.find("P") >= 0:
					time=str(int(time[0])+12)+":00 hrs"
			elif dig == 2:
				if time.find("a") >= 0 or time.find("h") >=0 or time.find("A") >= 0 or time.find("H") >=0:
					if time[0:2] == "12":
						time="00:00 hrs"
					else:
						time=time[0:2]+":00 hrs"
				elif time.find("p") >= 0 or time.find("P") >= 0:
					if time[0:2] == "12":
						time = "12:00 hrs"
					else:
						time = str(int(time[0:2]) + 12) + ":00 hrs"
			elif dig == 3:
				if time.find("a") >= 0 or time.find("h") >=0 or time.find("A") >= 0 or time.find("H") >=0:
					time="0"+time[0]+":"+time[2:4]+" hrs"
				elif time.find("p") >= 0 or time.find("P") >= 0:
					time = str(int(time[0])+12) + ":" + time[2:4] + " hrs"
				else:
					time = "0" + time[0] + ":" + time[2:4] + " hrs"
			elif dig == 4:
				if time.find("a") >= 0 or time.find("h") >=0 or time.find("A") >= 0 or time.find("H") >=0 or time.find("I") >=0:
					if time[0:2] == "12":
						 time = "00:"+time[3:5]+" hrs"
					else:
						 time=time[0:2]+":"+time[3:5]+" hrs"
				elif time.find("p") >= 0 or time.find("P") >= 0:
					if time[0:2] == "12":
						 time = "12:"+time[3:5]+" hrs"
					elif int(time[0:2]) > 12:
						time = time[0:2]+":"+time[3:5]+" hrs"
					else:
						 time = str(int(time[0:2]) + 12) + ":"+time[3:5]+" hrs"
				else:
					time = time[0:2]+":"+time[3:5]+" hrs"
			else:
				continue
			tim.append(time)
			time_pos.append(x)
		elif ent.label_ == "I-VENUE":                                                #i-venue entities
			cont_ven_pos.append(x)
			cont_ven.append(ent.text)
		elif ent.label_ == "B-LINK" and bool(re.fullmatch(patlink,ent.text)):          #b-link entities
			link_pos.append(x)
			link_list.append(ent.text)
		if ent.label_ == "B-VENUE" or ent.label_=="I-VENUE":                         #b and i venue entities
			combven_pos.append(x)
			combven.append(ent.text)
		x += 1

	if len(arr)==0:
		return 0
	if date_fun()==0:
		return 0
	else:
		fdate = str(date_fun()[0])
		min_pos_arr = date_fun()[1]
		ftime = time_fun()
		venue = venue_fun()
		flink = link_fun()

		if (len(link_pos) == 0 and (venue == "Webinar" or venue == "Hackathon" or venue == "Online session")):
			flink = "The link is in your mail"
		return fdate, ftime, venue, flink
	
def date_fun():
	pattern4 = r'(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:\.|\/|\-)(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:\.|\/|\-)(?:[1-2]\d{3})?'
	pattern2 = r'(?:(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)(?:,|\s)*)+(?:and|\s)*(?:0[1-9]|[1-9]|1[0-9]|2[0-9]|3[0-1])(?:st|nd|rd|th)(?:\s|of)*(?:[Jj][Aa][Nn][Uu][Aa][Rr][Yy]|[Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy]|[Mm][Aa][Rr][Cc][Hh]|[Aa][Pp][Rr][Ii][Ll]|[Mm][Aa][Yy]|[Jj][Uu][Nn][Ee]|[Jj][Uu][Ll][Yy]|[Aa][Uu][Gg][Uu][Ss][Tt]|[Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr]|[Oo][Cc][Tt][Oo][Bb][Ee][Rr]|[Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]|[Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]|[Jj][Aa][Nn]|[Ff][Ee][Bb]|[Mm][Aa][Rr]|[Aa][Pp][Rr]|[Mm][Aa][Yy]|[Jj][Uu][Nn]|[Jj][Uu][Ll]|[Aa][Uu][Gg]|[Ss][Ee][Pp]|[Oo][Cc][Tt]|[Nn][Oo][Vv]|[Dd][Ee][Cc])(?:(?:,|\s)*(?:[1-2]\d{3}))?'
	fdate = "31-12-3000"
	if (len(arr) > 0):
		min_pos_arr=[]
		datelist=[]
		min_date = "3000-12-31"
		flag = 1
		min_pos = 1000
		x=-1

		if len(arr) == [x.lower() for x in arr].count(arr[0].lower()) and arr[0].lower() == "today":  # "today" is only date
			flag = 0
			fdate = datetime.datetime.today().strftime("%d-%m-%Y")
			min_pos = position[0]
			min_pos_arr.append(min_pos)
		elif len(arr) == [x.lower() for x in arr].count(arr[0].lower()) and arr[0].lower() in ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]:  # "weekday" is only date
			flag = 0
			fdate = dateutil.parser.parse(arr[0].lower()).strftime("%d-%m-%Y")
			min_pos = position[0]
			min_pos_arr.append(min_pos)
		elif len(arr) == [x.lower() for x in arr].count(arr[0].lower()) and arr[0].lower() == "tomorrow":  # "tomorrow" is only date
			flag = 0
			fdate = (datetime.datetime.now() + datetime.timedelta(1)).strftime("%d-%m-%Y")
			min_pos = position[0]
			min_pos_arr.append(min_pos)
		else:
			for date in arr:
				td = datetime.datetime.today().strftime("%Y-%m-%d")
				x += 1
				if date.lower() in ["this sunday", "this monday", "this tuesday", "this wednesday", "this thursday",
									"this friday", "this saturday"]:  # weekdays
					nd = dateutil.parser.parse(date[5:]).strftime("%Y-%m-%d")
					if nd < min_date and nd >= td:
						min_date = nd
						min_pos = position[x]
					elif nd == min_date:
						min_pos_arr.append(position[x])
					datelist.append(nd)
					continue
				elif date.lower() in ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday",
									  "saturday"]:  # weekdays
					continue
				elif re.fullmatch('today', date, re.IGNORECASE):
					continue
				elif re.fullmatch('tomorrow', date, re.IGNORECASE):
					continue
				elif (date.lower()) == "this weekend":
					nd = dateutil.parser.parse("saturday").strftime("%Y-%m-%d")
					if nd < min_date and nd >= td:
						min_date = nd
						min_pos = position[x]
					elif nd == min_date:
						min_pos_arr.append(position[x])
					datelist.append(nd)
					continue
				elif (date.lower()) == "this week":
					nd = dateutil.parser.parse("monday").strftime("%Y-%m-%d")
					if nd < min_date and nd >= td:
						min_date = nd
						min_pos = position[x]
					elif nd == min_date:
						min_pos_arr.append(position[x])
					datelist.append(nd)
					continue
				elif bool(re.fullmatch(pattern4, date)):
					nd = date[6:] + "-" + date[3:5] + "-" + date[0:2]
					if nd < min_date and nd >= td:
						min_date = nd
						min_pos = position[x]
					elif nd == min_date:
						min_pos_arr.append(position[x])
					datelist.append(nd)
					continue
				try:
					table = str.maketrans(string.punctuation, ' ' * len(string.punctuation))
					final = date.lower().translate(table).split()
					year = datetime.datetime.now().year  # defaulting to current year incase of no year
					temp = final
					arb = 32
					for i in temp:
						j = "".join(list(i)[0:2])
						if (len(i) == 4 and i.isnumeric()):  # get year
							year = i
						elif (j.isnumeric()):
							if (int(j) < arb):
								arb = int(j)
						elif (j[0].isnumeric()):  # single digit date case
							if (int(j[0]) < arb):
								arb = int(j[0])
						elif (i in ['january', 'february', 'march', 'april', 'may', 'june', 'july', 'august', 'september', 'october', 'november', 'december', 'jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec']):
							month = i
						else:
							continue
					if bool(re.fullmatch(pattern2, date)):  # 3 or more date with year
						if (arb > 9):
							arb = str(arb)
						else:
							arb = "0" + str(arb)
						nd = arb + " " + month + " " + str(year)
						nd = dateutil.parser.parse(nd).strftime("%Y-%m-%d")
						if nd < min_date and nd >= td:
							min_date = nd
							min_pos = position[x]
						elif nd == min_date:
							min_pos_arr.append(position[x])
						datelist.append(nd)
					else:
						nd = dateutil.parser.parse(date).strftime("%d-%m-%Y")
						nd = nd.replace(nd[6:], str(year))
						if (arb > 9):
							nd = nd.replace(nd[0:2], str(arb), 1)
						else:
							arb = "0" + str(arb)
							nd = nd.replace(nd[0:2], str(arb), 1)
						nd = nd[6:] + "-" + nd[3:5] + "-" + nd[0:2]
						if(nd[8:]=='32'):
							continue
						if nd < min_date and nd >= td:
							min_date = nd
							min_pos = position[x]
						elif nd == min_date:
							min_pos_arr.append(position[x])
						datelist.append(nd)
				except:
					continue
		if (flag != 0):
			try:
				fdate = dateutil.parser.parse(min_date).strftime("%d-%m-%Y")
			except:
				fdate = "31-12-3000"
		if min_pos < 1000:
			min_pos_arr.append(min_pos)
		if fdate == "31-12-3000":
			min_pos_arr = []
		if len(min_pos_arr)>0:
			return fdate,min_pos_arr
		else:
			return 0

def time_fun():
	ftime=""
	if len(time_pos) > 0 and len(min_pos_arr) > 0:
		min_tim = 100
		for m in min_pos_arr:
			itr = m
			itr += 1
			while itr <= time_pos[len(tim) - 1]:
				if itr in time_pos:
					break
				itr += 1
			if itr <= time_pos[len(tim) - 1]:
				if min_tim >= (abs(m - itr)):
					min_tim = abs(m - itr)
					tpos = itr
					ftime = tim[time_pos.index(tpos)]
			else:
				itr = m - 1
				while (itr >= time_pos[0]):
					if itr in time_pos:
						break
					itr -= 1

				if itr >= time_pos[0]:
					pos = time_pos[tim.index(min(tim[0:time_pos.index(itr) + 1]))]
					if min_tim >= (abs(m - pos)):
						min_tim = abs(m - pos)
					ftime = min(tim[0:time_pos.index(itr) + 1])
	if ftime == "":
		ftime = "Not Specified"


	return ftime

def venue_fun():
	venue=""
	if len(min_pos_arr) > 0 and len(ven_pos) > 0:
		if "webinar" in [x.lower() for x in ven]:
			venue = "WEBINAR"
		else:
			min_dist = 100
			for m in min_pos_arr:
				absdiff = lambda list_value: abs(list_value - m)
				closest_value = min(combven_pos, key=absdiff)
				if min_dist >= (abs(m - closest_value)):
					min_dist = abs(m - closest_value)
					vpos = closest_value
			venue = combven[combven_pos.index(vpos)]
			if vpos in ven_pos:
				vpos += 1
				while (vpos) in cont_ven_pos:
					venue = venue + " " + cont_ven[cont_ven_pos.index(vpos)]
					vpos += 1
			else:
				vpos -= 1
				while (vpos in cont_ven_pos):
					venue = cont_ven[cont_ven_pos.index(vpos)] + " " + venue
					vpos -= 1
				if vpos in ven_pos:
					venue = ven[ven_pos.index(vpos)] + " " + venue
				else:
					for m in min_pos_arr:
						absdiff = lambda list_value: abs(list_value - m)
						closest_value = min(ven_pos, key=absdiff)
						if min_dist >= (abs(m - closest_value)):
							min_dist = abs(m - closest_value)
							vpos = closest_value
					venue = ven[ven_pos.index(vpos)]
					vpos += 1
					while (vpos) in cont_ven_pos:
						venue = venue + " " + cont_ven[cont_ven_pos.index(vpos)]
						vpos += 1

		if venue.upper() == "ONLINE":
			venue = "ONLINE SESSION"
		venue = venue.lower()
		venue = venue.capitalize()
		if venue == "":
			venue = "Not Specified"

	elif len(ven_pos) == 0:
		venue = "Not Specified"
	return venue

def link_fun():
	flink = ""
	if len(min_pos_arr) > 0 and len(ven_pos) > 0:
		if (("hackathon" in [x.lower() for x in ven]) or ("online" in [x.lower() for x in ven]) or (
				"webinar" in [x.lower() for x in ven])) and len(link_pos) >= 1:
			ml = 100
			for m in min_pos_arr:
				absdiff = lambda list_value: abs(list_value - m)
				closest_value = min(link_pos, key=absdiff)
				if ml >= (abs(m - closest_value)):
					ml = abs(m - closest_value)
					lpos = closest_value
			flink = link_list[link_pos.index(lpos)].lower()
	return flink
	



if __name__ == '__main__':
	app.run(debug=True)