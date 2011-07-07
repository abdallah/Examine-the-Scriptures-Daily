import time, os, shutil
import sqlite3

dbfile = os.path.join(os.getcwd(), 'db.sqlite3')
if os.path.isfile(dbfile):
    shutil.move(dbfile, dbfile+'.bak')
conn = sqlite3.connect(dbfile)
conn.text_factory = lambda x: unicode(x, "utf-8", "ignore")
c = conn.cursor()
c.execute('''create table esd11
 ("_id" INTEGER PRIMARY KEY, "date" TEXT NOT NULL  UNIQUE , "verse" TEXT NOT NULL , "comment" TEXT NOT NULL )''')
c.execute('''create table android_metadata
 ("locale" TEXT DEFAULT 'en_US')''')
c.execute('''INSERT INTO "android_metadata" VALUES ('en_US')''')
THIS_YEAR=2011


base_path = os.path.join(os.getcwd(), 'imports')
for f in os.listdir(base_path):
    if os.path.isfile(os.path.join(base_path, f)):
        fl = open(os.path.join(base_path, f), 'r')
        year=THIS_YEAR
        if (int(f[:2])>12):
            year=THIS_YEAR+1
        line = fl.readline().strip()
        counter = 0
        while line:
            line = fl.readline().strip()
            counter += 1
            #print counter, line
            if counter==1:
                if line=='': continue
                datestring = line.decode('ascii', 'ignore')
                _date = time.strptime(datestring+', '+unicode(year), "%A, %B%d, %Y")
                _date = time.strftime('%Y-%m-%d', _date)
                #print _date;
                continue
            if counter==2:
                _verse = line
                #print _verse;
                continue
            if counter==3:
                _comment = line
                #print _comment
                #print 'executing sql...'
                c.execute('insert into esd11 values (NULL,?,?,?)', (_date, _verse, _comment))
                conn.commit()
                counter = 0 
                
c.close()
conn.close()
