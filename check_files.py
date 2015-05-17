import glob, os

def get_filenames(subdir_name, file_type):
	file_list = []
	os.chdir("."+subdir_name)
	for file in glob.glob("*."+file_type):
		file_list.append(file)
	print file_list
	return file_list
