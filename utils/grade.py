import argparse
import re

parser = argparse.ArgumentParser()
parser.add_argument("FILE", help="Log file path")
args = parser.parse_args()

print("[Trace] Processing log file: {}".format(args.FILE))

pattern_enter = re.compile("\[Node (\d)\] Enter critical section")
pattern_leave = re.compile("\[Node (\d)\] Leave critical section")

try:
    with open(args.FILE, 'r', encoding="UTF-8") as f:
        id_enter = ""
        id_leave = ""
        cache = ""
        count = 0;
        for i, line in enumerate(f):
            count = i
            line = line.strip()
            # print('{} {}'.format(i + 1, line.strip()))

            # Even line, expected Enter message
            if i % 2 == 0:
                match = pattern_enter.match(line)
                if match:
                    id_enter = match.group(1)
                    cache = line
                else:
                    raise ValueError(
                        'Mis-ordered critical section request. Expected \'Enter\' event.\n'
                        ' at line {}: \n'
                        '            {}'.format(i + 1, line))

            else:
                match = pattern_leave.match(line)

                if match:
                    id_leave = match.group(1)
                    if id_leave != id_enter:
                        raise ValueError(
                            'Critical section execution mismatch\n'
                            ' at line {}: \n'
                            '           {}\n'
                            '           {}'.format(i + 1, cache, line))
                else:
                    raise ValueError(
                        'Mis-ordered critical section request.  Expected \'Leave\' event.\n'
                        ' at line {}: \n'
                        '           {}'.format(i + 1, line))

        if count % 2 == 0:
            raise ValueError('Missing critical section exit message\n'
                             'at line {}: \n'
                             '          {}'.format(count + 1, cache))
except ValueError as e:
    print("[Error] {}".format(str(e)))
    exit(1)
except FileNotFoundError as e:
    print(str(e))
    exit(1)

print("Congratulations! Your algorithm is correct :)")