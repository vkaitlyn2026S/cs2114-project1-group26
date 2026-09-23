# cs2114-project1-group26

**Health Bar** — a console Java fitness tracker. It shows four daily bars
(steps, sleep, calories, water), a Daily Health Score, a streak and one
coaching line, and it saves your log to a text file.

Team: Prateek, Nikitha, Kaitlyn

## Run it

Run `HealthBarApp.main`. On first launch it asks for your four daily goals
(press Enter to accept each suggested one). Your data is saved to
`healthbar.txt` in the working directory when you quit (option 6, or
Ctrl-D).

## Menu

1 Log a value · 2 Edit a value · 3 Delete a value · 4 Dashboard ·
5 History & averages · 6 Save & quit · 7 Export weekly summary

## Stretch goals included

- **Weekly summary export** (menu 7): writes `healthbar-week.txt` with the
  last 7 days, averages vs. goals, streak, badges and the coaching tip.
- **Badges** on the dashboard: *7-Day Streak* (7 days in a row scoring 50+),
  *First 10K Steps Day*, *Hydration Week* (7 days in a row at the water goal).
- **Goal auto-tuning** at start-up: if the 7 days before today all beat a
  goal, it offers to raise it 10%; if they all missed, it offers to lower it
  10%. Enter or "n" keeps the goal.

## Classes

| Class | Owner | Job |
|---|---|---|
| `HealthBarApp` | Prateek | Entry point and menu loop |
| `ConsoleUI` | Prateek | All console input and output |
| `InputParser` | Nikitha | Turns typed text into checked values |
| `HealthScorer` | Nikitha | Score, streak, 7-day averages, weakest metric |
| `Metric` | Kaitlyn | The four metrics and their constants |
| `DayLog` | Kaitlyn | One day's four values |
| `Goals` | Kaitlyn | The four daily targets |
| `HealthLog` | Kaitlyn | Every day, in date order |
| `LogFile` | Kaitlyn | Reading and writing `healthbar.txt` |

Each class has a matching `...Test` class (JUnit, `student.TestCase`).
To run the tests, the project needs `CS2-Support` (`student.jar`) on its
build path.

## Save file format

```
GOALS,10000,8,2000,8
09/09/2026,8400,7.5,2100,6
```

One `GOALS` line, then one line per day: the date, then steps, sleep,
calories and water. Bad lines are skipped and counted on load.
