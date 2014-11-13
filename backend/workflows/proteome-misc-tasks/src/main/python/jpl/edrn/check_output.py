'''
Script to check the existence of myrimatch output files.
'''

import os

PRODUCTS_FILE='/usr/local/edrn/products.txt'
PRODUCTS_DIR='/data/staging/20140506-Pancreatic-Cyst-Fluid'
PRODUCT_EXT = '.pepXML'

def readDbFile():
    '''Reads the list of products already submitted.'''
    
    with open(PRODUCTS_FILE) as f:
        products = f.read().splitlines()
        
    return products
    
def listDirFiles():
    files = []
    for name in os.listdir(PRODUCTS_DIR):
        if os.path.isfile(os.path.join(PRODUCTS_DIR, name)):
            _name, ext = os.path.splitext(name)
            if ext == PRODUCT_EXT:
                files.append(name)
    return files 
    
if __name__ == '__main__':
    
    products = readDbFile()
    files = listDirFiles()
    
    for product in products:
        pfile = '%s-MM.pepXML' % product
        if pfile not in files:
            print 'MISSING PRODUCT: %s' % product
