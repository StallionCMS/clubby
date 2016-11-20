#!python
import os
import sys
from plumbum import FG, BG, local, TEE, TF

args = []
if '--skip-tests' in sys.argv:
    args.append('-DskipTests=true')
if '--jenkins' in sys.argv:
    args.append('-DstallionEnv=jenkins')
args = tuple(args) + ('compile', 'package', 'assembly:single', 'install')
local['mvn'][args] & FG

script = '''#!/bin/sh
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
exec java -classpath "$DIR/../jars/*"  -jar $0 "$@"


'''
with open('./target/clubhouse-stallion-app', 'w') as f:
    f.write(script)



(local['cat']['./target/clubhouse-1.0-SNAPSHOT-jar-with-dependencies.jar'] >> './target/clubhouse-stallion-app') & FG
os.chmod('./target/clubhouse-stallion-app', 0o700)
if not os.path.isdir('../site/bin'):
    os.makedirs('../site/bin')
local['cp']['./target/clubhouse-stallion-app', '../site/bin/clubhouse-stallion-app'] & FG
