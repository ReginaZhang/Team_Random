# a function to insert customer's data into the database
def insert_custbl(db,form, cursor):
    
    sql=""" INSERT INTO Customer ( Phone, CustomerType) VALUES ('%s', 'I')""" % form.getfirst("contact")

    cursor.execute(sql)
    db.commit()

              
# a function to insert customer's data into the database for every customer 
def insert_indtbl(db,form, cursor, cust_id):               

    cursor.execute("""INSERT INTO Individual(CustID, FirstName, MiddleName, LastName, Greder,DoB, Height,Weight)            VALUES (%s, %s, %s, %s, %s, %s,%s)""",(cust_id, form.getfirst("firstname"),
      form.getfirst("middlename"), 
      form.getfirst("lastname"),
      form.getfirst("gerder"),
      form.getfirst("dob"),
      form.getfirst("height"),
      form.getfirst("weight")))
    db.commit()

# a function to insert address of the customer into the database
def insert_address(db,form, cursor):
    cursor.execute("""INSERT INTO Address(StreetNumber, StreetName, StreetType, MajorMunicapility, GoverningDistrict, PostalArea,Country) VALUES(%s, %s,%s,%s,%s,%s,%s)""",(form["streetnum"].value, form["street"].value,     form["streettype"].value,form["major"].value, form["district"].value, form["postal"].value,form.getfirst("country")))
    db.commit()                           

# a function to insert customer's address status to the CustomerAddress
def insert_custaddtbl(db,form, cursor, cust_id, ad_id, now):
    cust_add="""INSERT INTO CustomerAddress(CustID, AddressID, StartDate) VALUES(%d, %d, "%s")"""% (int(cust_id), int(ad_id), now.strftime("%Y-%m-%d"))

    cursor.execute(cust_add)
    db.commit()

# a function to record the username and password have been signed up    
def inser_usertbl(db,form, cursor, cust_id):    
    bb_user="""INSERT INTO bb_reg_user(user_name, userID, user_password, user_type) VALUES("%s", %d, "%s", "customer")"""% (form["username"].value, int(cust_id), form["password"].value)

    cursor.execute(bb_user)
    db.commit()



# the main function, handle all the process
def main():
    too_many_arguments = 0
    try:
        sess = session.Session(expires=20*60, cookie_path='/')
        print "%s\nContent-Type: text/html\n" % (sess.cookie)
    

        form=cgi.FieldStorage()
        db = MySQLdb.connect(host="", user="",passwd="",db="", port=)

        cursor = db.cursor()
        # if there is error for the data given
        if(form.getfirst("firstname")!=None):
            too_many_arguments=1
            cursor.close()
            db.close()
            sess.close()
          else:
            insert_custbl(db,form, cursor)
            cust_id=cursor.lastrowid
            
            insert_indtbl(db,form, cursor, cust_id)
                                                                                                                                                     
            insert_address(db,form, cursor)
            
            ad_id=cursor.lastrowid
            now=datetime.datetime.now()
            insert_custaddtbl(db,form, cursor, cust_id, ad_id, now) 
            
            inser_usertbl(db,form, cursor, cust_id)
            
            # give data to the session for further access  
            cursor.execute ("""    
            SELECT *
            FROM  reg_user
            WHERE user_name = %s
              AND user_password = %s
        """,(form["username"].value, form["password"].value))
            sess.data['loggedIn'] = 1
            sess.data['userName'] = form["username"].value
            
            cursor.close()
            db.close()
            sess.close()
        if( too_many_arguments==1):
            whereToNext="....?paramName=tooMany"
        else:
            whereToNext = "..." 
    except MySQLdb.Error, e:
        whereToNext="...?paramName=ErrorPage"

    
# calling the main function
main()           
                                                                                                 
                                                                                         

​