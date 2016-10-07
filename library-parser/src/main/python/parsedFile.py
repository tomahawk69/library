# py -m pip install --upgrade pymongo
import sys
from pymongo import MongoClient, ASCENDING, DESCENDING

client = MongoClient('localhost', 27017)

db = client.library


def find_last_lib():
    print("find last library")
    try:
        docs = db.library.find({}).sort('updated', DESCENDING)
        if docs.count() > 0:
            return docs[0]['_id']
        else:
            return None
    except:
        print("Error:", sys.exc_info()[1])


def list_files(count=10):
    print('find first {} items from {}'.format(count, libId))
    files = db['files_' + str(libId)]
    print('collection has {} elements'.format(files.count()))

    try:
        for f in files.find({}, {"cover.bytes": False, "section": False}).sort('_id'):
            print(f)

    except:
        print("Error:", sys.exc_info()[1])



libId = find_last_lib()
print('found library: ', libId)

list_files()

# find_one(10)
# find_many()
