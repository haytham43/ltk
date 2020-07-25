## LLRP Toolkit (LTK)

This is an exact copy of the original CVS project.

For more information, check out: https://sourceforge.net/projects/llrp-toolkit

### Import LTK into git
1. get the original CVS project
```bash
mkdir /home/user/SF_LTK
cd /home/user/SF_LTK
cvs -z3 -d:pserver:anonymous@llrp-toolkit.cvs.sourceforge.net:/cvsroot/llrp-toolkit co -P LTK
```

2. generate blob and dump files
```bash
mkdir /home/user/cvs2git-tmp
cd /home/user/cvs2git-tmp
cvs2git --blobfile=blob.dat --dumpfile=dump.dat \
        --username=USERNAME --default-eol=native \
        --encoding=utf8 --encoding=latin1 --fallback-encoding=ascii \
        /home/user/SF_LTK/LTK
```

4. prepare a fresh git repo
```bash
mkdir /home/user/GIT_LTK
cd /home/user/GIT_LTK; git init
```

5. import
```bash
cat /home/user/cvs2git-tmp/{blob.dat,dump.dat} | git --git-dir=/home/user/GIT_LTK/.git fast-import
```

6. cleanup
```bash
rm -r /home/user/SF_LTK
rm -r /home/user/cvs2git-tmp
```

### Resources
* https://sourceforge.net/p/llrp-toolkit/code/
* https://sourceforge.net/p/forge/documentation/CVS/
* https://launchpad.net/ubuntu/+source/cvs2svn/2.5.0-1
