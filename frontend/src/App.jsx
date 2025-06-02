import React, { useState } from "react";
import Papa from "papaparse";

function App() {
  const [csvFile, setCsvFile] = useState(null);
  const [studentList, setStudentList] = useState([]);
  const [groupCount, setGroupCount] = useState(2);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [results, setResults] = useState(null);

  // Handle CSV upload and parse
  const handleCsvChange = (e) => {
    setCsvFile(e.target.files[0]);
    setError(""); // Clear previous errors when new file is selected
  };

  const handleParseCsv = () => {
    if (!csvFile) {
      setError("Please select a CSV file first.");
      return;
    }
    setError("");

    Papa.parse(csvFile, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => {
        try {
          // Check if CSV has exactly the required headers
          const headers = results.meta.fields || [];

          // Strict validation: must have exactly "name" and "grade" columns
          const normalizedHeaders = headers.map((h) => h.toLowerCase().trim());
          const hasExactlyNameAndGrade =
            normalizedHeaders.length === 2 &&
            normalizedHeaders.includes("name") &&
            normalizedHeaders.includes("grade");

          if (!hasExactlyNameAndGrade) {
            setError(
              `CSV must contain exactly 'name' and 'grade' columns only. Found: ${headers.join(
                ", "
              )}`
            );
            return;
          }

          // Process the data with validation
          const processedData = results.data
            .map((row, index) => {
              const name = (row["name"] || row["Name"] || "").toString().trim();
              const gradeStr = (row["grade"] || row["Grade"] || "")
                .toString()
                .trim();
              const grade = parseFloat(gradeStr);

              // Skip completely empty rows
              if (!name && !gradeStr) return null;

              // Validate required fields
              if (!name) {
                throw new Error(
                  `Row ${
                    index + 2
                  }: Student name is required and cannot be empty`
                );
              }

              if (!gradeStr || isNaN(grade)) {
                throw new Error(
                  `Row ${index + 2}: Valid grade is required for ${name}`
                );
              }

              // Grade range validation
              if (grade < 0 || grade > 100) {
                throw new Error(
                  `Row ${
                    index + 2
                  }: Grade must be between 0 and 100 for ${name}`
                );
              }

              return { name, grade };
            })
            .filter((item) => item !== null);

          if (processedData.length === 0) {
            setError("No valid student data found in CSV");
            return;
          }

          // Check for duplicate names
          const names = processedData.map((s) => s.name.toLowerCase());
          const duplicates = names.filter(
            (name, index) => names.indexOf(name) !== index
          );
          if (duplicates.length > 0) {
            setError(
              `Duplicate student names found: ${[...new Set(duplicates)].join(
                ", "
              )}`
            );
            return;
          }

          setStudentList(processedData);
          setError("");
        } catch (err) {
          setError(err.message);
        }
      },
      error: (error) => {
        setError(`Failed to parse CSV: ${error.message}`);
      },
    });
  };

  // Enhanced submit validation
  const handleSubmit = async () => {
    // Clear previous errors and results
    setError("");
    setResults(null);

    // Validate student list
    if (studentList.length === 0) {
      setError("Please upload and parse a CSV file with student data first.");
      return;
    }

    // Validate group count
    if (!groupCount || groupCount < 2) {
      setError("Number of groups must be at least 2.");
      return;
    }

    if (groupCount > studentList.length) {
      setError(
        `Number of groups (${groupCount}) cannot exceed the number of students (${studentList.length}).`
      );
      return;
    }

    // Validate that each student has valid data
    const invalidStudents = studentList.filter(
      (student) =>
        !student.name ||
        student.name.trim() === "" ||
        isNaN(student.grade) ||
        student.grade === null ||
        student.grade === undefined
    );

    if (invalidStudents.length > 0) {
      setError("Some students have invalid data. Please check the CSV file.");
      return;
    }

    setLoading(true);

    try {
      const res = await fetch(
        `https://fairgroupassignment-production.up.railway.app/api/assign/${groupCount}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(studentList),
        }
      );

      if (!res.ok) {
        const errorText = await res.text().catch(() => "Unknown error");
        throw new Error(
          `Failed to assign groups: ${res.status} ${res.statusText}. ${errorText}`
        );
      }

      const data = await res.json();

      // Validate response data
      if (!data || !Array.isArray(data) || data.length === 0) {
        throw new Error(
          "Invalid response from server: No group data received."
        );
      }

      setResults(data);
    } catch (err) {
      setError(
        err.message || "An unexpected error occurred while creating groups."
      );
    } finally {
      setLoading(false);
    }
  };

  // Handle group count changes with validation
  const handleGroupCountChange = (e) => {
    const value = e.target.value;

    // Allow empty string for user to clear field
    if (value === "") {
      setGroupCount("");
      return;
    }

    const count = Number(value);
    if (count >= 1) {
      setGroupCount(count);
      setError(""); // Clear error if valid
    }
  };

  return (
    <div className="min-h-screen max-h-screen flex flex-col bg-gradient-to-br from-indigo-100 via-blue-50 to-white overflow-hidden">
      {/* Header */}
      <header className="w-full bg-white/70 backdrop-blur border-b border-blue-100 shadow-sm flex-shrink-0">
        <div className="w-full flex items-center justify-between px-3 sm:px-4 lg:px-6 py-2 sm:py-3">
          <div className="flex items-center gap-2 sm:gap-3">
            <svg
              className="w-5 h-5 sm:w-6 sm:h-6 lg:w-7 lg:h-7 text-indigo-500 flex-shrink-0"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v1a3 3 0 01-3 3H7a3 3 0 01-3-3v-1m13 0a3 3 0 00-3-3H7a3 3 0 00-3 3m13 0V7a4 4 0 00-4-4H7a4 4 0 00-4 4v13"
              />
            </svg>
            <span className="text-xl sm:text-2xl lg:text-3xl font-black tracking-tight text-indigo-700">
              Fair Group Assignment
            </span>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 overflow-hidden">
        <div className="h-full grid grid-cols-1 xl:grid-cols-2 gap-0 xl:gap-4 p-0 xl:p-4">
          {/* Input Panel */}
          <div className="bg-white/80 backdrop-blur-lg shadow-xl flex flex-col border-b xl:border-b-0 xl:border border-blue-100 xl:rounded-3xl overflow-hidden">
            <div className="p-3 sm:p-4 lg:p-6 xl:p-8 flex-1 overflow-y-auto">
              {/* CSV Upload Section */}
              <div className="border-2 border-dashed border-indigo-200 rounded-xl sm:rounded-2xl p-4 sm:p-6 lg:p-8 text-center mb-3 sm:mb-4 bg-indigo-50/50">
                <input
                  type="file"
                  accept=".csv"
                  className="hidden"
                  id="csv-upload"
                  onChange={handleCsvChange}
                />
                <label
                  htmlFor="csv-upload"
                  className="cursor-pointer text-indigo-700 font-semibold text-sm sm:text-base block"
                >
                  {csvFile ? csvFile.name : "Drop or click to upload CSV"}
                </label>
                <button
                  className="mt-3 sm:mt-4 px-4 sm:px-5 py-2 bg-indigo-600 text-white rounded-lg sm:rounded-xl text-sm sm:text-base font-semibold hover:bg-indigo-700 shadow disabled:opacity-50"
                  onClick={handleParseCsv}
                  disabled={!csvFile}
                >
                  Parse CSV
                </button>
              </div>
              <div className="text-xs text-gray-500 mb-4">
                <div className="bg-indigo-100 rounded-lg p-3">
                  <div className="font-semibold mb-1">Required CSV Format:</div>
                  <div className="font-mono text-xs bg-white px-2 py-1 rounded border">
                    name,grade
                    <br />
                    John Doe,85
                    <br />
                    Jane Smith,92
                  </div>
                  <div className="text-xs mt-2 text-gray-600">
                    • Must contain exactly "name" and "grade" columns only
                  </div>
                </div>
              </div>

              {/* Students List */}
              <div className="mt-3 sm:mt-4 mb-3 sm:mb-4 flex-1 min-h-0">
                {studentList.length > 0 ? (
                  <div className="bg-indigo-50/60 rounded-xl shadow-sm max-h-40 sm:max-h-48 lg:max-h-60 overflow-y-auto">
                    <div className="px-3 py-2 bg-indigo-100 rounded-t-xl">
                      <span className="text-sm font-semibold text-indigo-800">
                        Students Loaded: {studentList.length}
                      </span>
                    </div>
                    <ul className="divide-y divide-indigo-100">
                      {studentList.map((s, i) => (
                        <li
                          key={i}
                          className="flex items-center justify-between px-3 py-2"
                        >
                          <span className="font-medium text-indigo-900 text-sm sm:text-base truncate pr-2">
                            {s.name}{" "}
                            <span className="text-xs text-gray-500">
                              ({s.grade})
                            </span>
                          </span>
                        </li>
                      ))}
                    </ul>
                  </div>
                ) : (
                  <div className="text-gray-400 text-sm text-center py-8">
                    No students loaded. Please upload and parse a CSV file.
                  </div>
                )}
              </div>

              {/* Group Count Input */}
              <div className="flex items-center gap-2 mb-4 sm:mb-6 lg:mb-8">
                <label className="text-sm font-semibold text-indigo-700 whitespace-nowrap">
                  Groups:
                </label>
                <input
                  className="w-16 sm:w-20 border border-blue-200 rounded-lg px-2 py-1 text-sm sm:text-base focus:ring-2 focus:ring-indigo-200 outline-none shadow-sm"
                  type="number"
                  min={2}
                  value={groupCount}
                  onChange={handleGroupCountChange}
                />
              </div>

              {/* Submit Button */}
              <button
                className="w-full py-4 bg-gradient-to-r from-indigo-600 to-blue-500 text-white rounded-2xl sm:rounded-3xl font-black text-lg sm:text-xl shadow-xl hover:from-indigo-700 hover:to-blue-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                onClick={handleSubmit}
                disabled={
                  studentList.length === 0 ||
                  loading ||
                  !groupCount ||
                  groupCount < 2
                }
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg
                      className="w-5 h-5 animate-spin"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      ></circle>
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8v8z"
                      ></path>
                    </svg>
                    Assigning...
                  </span>
                ) : (
                  "Create Groups"
                )}
              </button>

              {/* Error Message */}
              {error && (
                <div className="mt-4 sm:mt-5 text-red-700 text-base text-center font-bold bg-red-100 rounded-2xl py-3 px-4 border-2 border-red-200">
                  {error}
                </div>
              )}
            </div>
          </div>

          {/* Output Panel */}
          <div className="bg-white/80 backdrop-blur-lg shadow-xl flex flex-col border-t xl:border-t-0 xl:border border-blue-100 xl:rounded-3xl overflow-hidden">
            <div className="p-3 sm:p-4 lg:p-6 xl:p-8 flex flex-col h-full min-h-0">
              <div className="flex-shrink-0 mb-3 sm:mb-4">
                <h2 className="text-lg sm:text-xl lg:text-2xl font-extrabold text-indigo-800 flex items-center gap-2">
                  <svg
                    className="w-5 h-5 sm:w-6 sm:h-6 text-indigo-400"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v1a3 3 0 01-3 3H7a3 3 0 01-3-3v-1m13 0a3 3 0 00-3-3H7a3 3 0 00-3 3m13 0V7a4 4 0 00-4-4H7a4 4 0 00-4 4v13"
                    />
                  </svg>
                  Assignment Results
                </h2>
                <div className="text-sm text-gray-500">
                  View group assignments
                </div>
              </div>

              {/* Results Content */}
              <div className="flex-1 min-h-0 overflow-y-auto">
                {!results && (
                  <div className="h-full flex flex-col items-center justify-center text-gray-400">
                    <svg
                      className="w-12 h-12 sm:w-16 sm:h-16 mb-2"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1.5"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v1a3 3 0 01-3 3H7a3 3 0 01-3-3v-1m13 0a3 3 0 00-3-3H7a3 3 0 00-3 3m13 0V7a4 4 0 00-4-4H7a4 4 0 00-4 4v13"
                      />
                    </svg>
                    <div className="font-semibold text-sm sm:text-base">
                      No Groups Assigned
                    </div>
                    <div className="text-xs mt-1 text-center px-4">
                      Upload student data and start the assignment process to
                      see results here.
                    </div>
                  </div>
                )}
                {results && (
                  <div className="space-y-4 sm:space-y-6 lg:space-y-8">
                    {results.map((group, idx) => (
                      <div
                        key={idx}
                        className="border-b pb-3 sm:pb-4 last:border-b-0 last:pb-0"
                      >
                        <div className="font-bold text-indigo-700 mb-1 text-base sm:text-lg flex items-center gap-2">
                          <svg
                            className="w-4 h-4 sm:w-5 sm:h-5 text-indigo-400"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v1a3 3 0 01-3 3H7a3 3 0 01-3-3v-1m13 0a3 3 0 00-3-3H7a3 3 0 00-3 3m13 0V7a4 4 0 00-4-4H7a4 4 0 00-4 4v13"
                            />
                          </svg>
                          Group {group.groupNumber}
                        </div>
                        <div className="text-xs text-gray-500 mb-2">
                          Average Grade:{" "}
                          <span className="font-mono">
                            {group.averageGrade}
                          </span>
                        </div>
                        <ul className="ml-2 space-y-1">
                          {group.students.map((s, i) => (
                            <li key={i} className="text-sm text-gray-700">
                              {s.name}{" "}
                              <span className="text-xs text-gray-400">
                                ({s.grade})
                              </span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full text-center text-xs text-gray-400 py-2 sm:py-4 flex-shrink-0 border-t border-blue-100">
        &copy; {new Date().getFullYear()} Fair Group Assignment. All rights
        reserved.
      </footer>
    </div>
  );
}

export default App;
