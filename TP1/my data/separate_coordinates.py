file = open('session_136031.kml.csv','r')
i = 0
j = 0
new_lines = []
for line in file.readlines():
    if i%2==0:
        new_lines.append(line)
    else:
        new_lines[j] = new_lines[j] + ',' + line
        print(new_lines[j])
        j += 1
    i += 1