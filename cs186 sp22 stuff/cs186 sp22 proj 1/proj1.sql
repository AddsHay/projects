-- Before running drop any existing views
DROP VIEW IF EXISTS q0;
DROP VIEW IF EXISTS q1i;
DROP VIEW IF EXISTS q1ii;
DROP VIEW IF EXISTS q1iii;
DROP VIEW IF EXISTS q1iv;
DROP VIEW IF EXISTS q2i;
DROP VIEW IF EXISTS q2ii;
DROP VIEW IF EXISTS q2iii;
DROP VIEW IF EXISTS q3i;
DROP VIEW IF EXISTS q3ii;
DROP VIEW IF EXISTS q3iii;
DROP VIEW IF EXISTS q4i;
DROP VIEW IF EXISTS q4ii;
DROP VIEW IF EXISTS q4iii;
DROP VIEW IF EXISTS q4iv;
DROP VIEW IF EXISTS q4v;

-- Question 0
CREATE VIEW q0(era)
AS
  SELECT MAX(era)
  FROM pitching
;

-- Question 1i
CREATE VIEW q1i(namefirst, namelast, birthyear)
AS
  SELECT namefirst, namelast, birthyear
  FROM people
  WHERE weight > 300
;

-- Question 1ii
CREATE VIEW q1ii(namefirst, namelast, birthyear)
AS
  SELECT namefirst, namelast, birthyear
  FROM people
  WHERE namefirst LIKE '% %'
  ORDER BY namefirst, namelast
;

-- Question 1iii
CREATE VIEW q1iii(birthyear, avgheight, count)
AS
  SELECT birthyear, AVG(height) AS avgheight, COUNT(*) AS count
  FROM people
  GROUP BY birthyear
  ORDER BY birthyear
;

-- Question 1iv
CREATE VIEW q1iv(birthyear, avgheight, count)
AS
  SELECT birthyear, AVG(height) AS avgheight, COUNT(*) AS count
  FROM people
  GROUP BY birthyear
  HAVING AVG(height) > 70
  ORDER BY birthyear
;

-- Question 2i
CREATE VIEW q2i(namefirst, namelast, playerid, yearid)
AS
  SELECT namefirst, namelast, hof.playerid, yearid
  FROM HallofFame AS hof, people AS p
  WHERE hof.playerid = p.playerid AND hof.inducted = 'Y'
  ORDER BY yearid DESC, hof.playerid
;

-- Question 2ii
CREATE VIEW q2ii(namefirst, namelast, playerid, schoolid, yearid)
AS
  SELECT namefirst, namelast, p.playerid, c.schoolid, hof.yearid
  FROM HallofFame AS hof, people AS p, CollegePlaying AS c, Schools AS s
  WHERE p.playerid = hof.playerid 
  AND p.playerid = c.playerid
  AND c.schoolid = s.schoolid
  AND s.schoolstate = 'CA'
  AND hof.inducted = 'Y'
  ORDER BY hof.yearid DESC, c.schoolID , p.playerid 
;

-- Question 2iii
CREATE VIEW q2iii(playerid, namefirst, namelast, schoolid)
AS
  SELECT p.playerid, namefirst, namelast, schoolid
  FROM people AS p
  INNER JOIN HallofFame AS hof ON p.playerid = hof.playerid
  LEFT JOIN CollegePlaying AS c ON p.playerid = c.playerid
  WHERE hof.inducted = 'Y'
  ORDER BY p.playerid DESC, schoolid
;

-- Question 3i
CREATE VIEW q3i(playerid, namefirst, namelast, yearid, slg)
AS
  SELECT p.playerid, namefirst, namelast, yearid, CAST((1 * (h - h2b - h3b - hr) + 2 * h2b + 3 * h3b + 4 * hr) AS float) / CAST(ab AS float) AS slg
  FROM people AS p, batting AS b
  WHERE p.playerid = b.playerid AND b.ab > 50
	ORDER BY slg DESC, b.yearid, p.playerid
	LIMIT 10
;

-- Question 3ii
CREATE VIEW q3ii(playerid, namefirst, namelast, lslg)
AS
  SELECT p.playerid, namefirst, namelast, CAST((1 * SUM(h - h2b - h3b - hr) + 2 * SUM(h2b) + 3 * SUM(h3b) + 4 * SUM(hr)) AS float) / CAST(SUM(ab) AS float) AS lslg
  FROM people AS p, batting AS b
  WHERE p.playerid = b.playerid
  GROUP BY p.playerid, namefirst, namelast
  HAVING SUM(ab) > 50
  ORDER BY lslg DESC, p.playerid
  LIMIT 10;
;

-- Question 3iii
CREATE VIEW q3iii(namefirst, namelast, lslg)
AS
  SELECT namefirst, namelast, CAST((1 * SUM(h - h2b - h3b - hr) + 2 * SUM(h2b) + 3 * SUM(h3b) + 4 * SUM(hr)) AS float) / CAST(SUM(ab) AS float) AS lslg
  FROM people AS p, batting AS b
  WHERE p.playerid = b.playerid
  GROUP BY namefirst, namelast, p.playerid
  HAVING (CAST((1 * SUM(h - h2b - h3b - hr) + 2 * SUM(h2b) + 3 * SUM(h3b) + 4 * SUM(hr)) AS float) / CAST(SUM(ab) AS float)) >
    (SELECT CAST((1 * SUM(h - h2b - h3b - hr) + 2 * SUM(h2b) + 3 * SUM(h3b) + 4 * SUM(hr)) AS float) / CAST(SUM(ab) AS float) AS lslg
    FROM people AS p, batting AS b
	  WHERE p.playerid = b.playerid
	  AND p.playerid = 'mayswi01'
	  GROUP BY p.playerid)
  AND SUM(ab) > 50   
;

-- Question 4i
CREATE VIEW q4i(yearid, min, max, avg)
AS
  SELECT yearid, MIN(salary) AS min, MAX(salary) AS max, AVG(salary) AS avg
  FROM salaries
  GROUP BY yearid
  ORDER BY yearid
;

-- Question 4ii
CREATE VIEW q4ii(binid, low, high, count)
AS
  WITH bins(salary, interval, bin) AS
    (SELECT salary, (SELECT (MAX(salary) - MIN(salary)) / 10.0 FROM salaries WHERE yearid = 2016) AS interval,
    CASE WHEN salary = (SELECT MAX(salary) FROM salaries WHERE yearid = 2016) THEN 9 
    ELSE FLOOR((salary - (SELECT MIN(salary) FROM salaries WHERE yearid = 2016)) / (SELECT (MAX(salary) - MIN(salary)) / 10.0 FROM salaries WHERE yearid = 2016)) 
    END AS bin FROM salaries WHERE yearid = 2016)
  SELECT CAST(bin AS int) AS binid, (SELECT MIN(salary) FROM bins WHERE bin = 0) + (bin * interval) as low,
    (SELECT MIN(salary) FROM bins WHERE bin = 0) + ((bin + 1) * interval) as high, COUNT(*) AS count
  FROM bins
  GROUP BY binid, interval
  ORDER BY binid
;

-- Question 4iii
CREATE VIEW q4iii(yearid, mindiff, maxdiff, avgdiff)
AS
  WITH temp AS (SELECT yearid, min(salary) AS min, max(salary) AS max, avg(salary) AS a 
    FROM salaries 
    GROUP BY yearid)
  SELECT * FROM
    (SELECT yearid, min - LAG(min, 1) OVER (ORDER BY yearid) AS mindiff,
      max - LAG(max, 1) OVER (ORDER BY yearid) AS maxdiff,
      a - LAG(a, 1) OVER (ORDER BY yearid) AS avgdiff
      FROM temp)
  WHERE mindiff IS NOT NULL AND maxdiff IS NOT NULL AND avgdiff IS NOT NULL 
;

-- Question 4iv
CREATE VIEW q4iv(playerid, namefirst, namelast, salary, yearid)
AS
SELECT p.playerid, namefirst, namelast, s.salary, s.yearid
  FROM (SELECT playerid, salary FROM salaries
    WHERE salary = (SELECT max(salary) from salaries WHERE yearid = 2000) AND yearid = 2000) 
    AS temp, salaries AS s, people as p
    WHERE temp.playerid = s.playerid AND p.playerid = s.playerid AND yearid = 2000  
UNION
SELECT p.playerid, namefirst, namelast, s.salary, s.yearid
  FROM (SELECT playerid, salary 
    FROM salaries 
    WHERE salary = (SELECT max(salary) from salaries WHERE yearid = 2001) AND yearid = 2001) 
    AS temp, salaries AS s, people as p
    WHERE temp.playerid = s.playerid AND p.playerid = s.playerid AND yearid = 2001
;
-- Question 4v
CREATE VIEW q4v(team, diffAvg) AS
  SELECT temp.teamid AS team, max(salary) - min(salary) AS diffAvg FROM
    (SELECT asf.playerid, asf.teamid, s.salary FROM allstarfull as asf 
      INNER JOIN salaries AS s ON asf.playerid = s.playerId AND asf.yearid = 2016 AND s.yearid = 2016) AS temp
  GROUP BY temp.teamid
;

