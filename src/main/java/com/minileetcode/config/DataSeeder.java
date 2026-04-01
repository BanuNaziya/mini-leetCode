package com.minileetcode.config;

import com.minileetcode.model.Problem;
import com.minileetcode.model.Submission;
import com.minileetcode.model.User;
import com.minileetcode.repository.ProblemRepository;
import com.minileetcode.repository.SubmissionRepository;
import com.minileetcode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DataSeeder - populates the database with sample data on startup.
 *
 * <p>Implements CommandLineRunner so it executes after the application context
 * is fully loaded. Guards against duplicate seeding by checking if data
 * already exists before inserting.
 *
 * <p>Seeds:
 * <ul>
 *   <li>5 users (1 admin + 4 regular users)</li>
 *   <li>10 problems (mix of EASY, MEDIUM, HARD)</li>
 *   <li>20 submissions across various users and problems</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository       userRepository;
    private final ProblemRepository    problemRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public void run(String... args) {
        // Only seed if the tables are empty - prevents duplicate data on restarts
        if (userRepository.count() == 0) {
            log.info("Seeding users...");
            seedUsers();
        } else {
            log.info("Users already exist - skipping user seed.");
        }

        if (problemRepository.count() == 0) {
            log.info("Seeding problems...");
            seedProblems();
        } else {
            log.info("Problems already exist - skipping problem seed.");
        }

        if (submissionRepository.count() == 0) {
            log.info("Seeding submissions...");
            seedSubmissions();
        } else {
            log.info("Submissions already exist - skipping submission seed.");
        }

        log.info("Data seeding complete. Visit http://localhost:8080 to explore the app.");
    }

    // ----------------------------------------------------------------
    //  Seed methods
    // ----------------------------------------------------------------

    private void seedUsers() {
        List<User> users = List.of(
            createUser("admin",    "admin@minileetcode.com",   "admin123",    User.Role.ADMIN, 42, "Expert"),
            createUser("alice",    "alice@example.com",        "password123", User.Role.USER,  18, "Intermediate"),
            createUser("bob",      "bob@example.com",          "password123", User.Role.USER,  7,  "Apprentice"),
            createUser("charlie",  "charlie@example.com",      "password123", User.Role.USER,  3,  "Beginner"),
            createUser("diana",    "diana@example.com",        "password123", User.Role.USER,  55, "Advanced")
        );
        userRepository.saveAll(users);
        log.info("Seeded {} users.", users.size());
    }

    private void seedProblems() {
        List<Problem> problems = List.of(
            createProblem(
                "Two Sum",
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.",
                Problem.Difficulty.EASY,
                "array,hash-table",
                "nums = [2,7,11,15], target = 9",
                "[0,1]",
                "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9",
                67.3, 5420
            ),
            createProblem(
                "Reverse Linked List",
                "Given the head of a singly linked list, reverse the list, and return the reversed list.",
                Problem.Difficulty.EASY,
                "linked-list,recursion",
                "head = [1,2,3,4,5]",
                "[5,4,3,2,1]",
                "The number of nodes in the list is in the range [0, 5000]\n-5000 <= Node.val <= 5000",
                72.1, 3210
            ),
            createProblem(
                "Valid Parentheses",
                "Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nAn input string is valid if:\n1. Open brackets must be closed by the same type of brackets.\n2. Open brackets must be closed in the correct order.",
                Problem.Difficulty.EASY,
                "stack,string",
                "s = \"()[]{}\"",
                "true",
                "1 <= s.length <= 10^4\ns consists of parentheses only '()[]{}'",
                65.8, 4100
            ),
            createProblem(
                "Longest Substring Without Repeating Characters",
                "Given a string s, find the length of the longest substring without repeating characters.",
                Problem.Difficulty.MEDIUM,
                "hash-table,string,sliding-window",
                "s = \"abcabcbb\"",
                "3",
                "0 <= s.length <= 5 * 10^4\ns consists of English letters, digits, symbols and spaces.",
                33.8, 9870
            ),
            createProblem(
                "Merge Two Sorted Lists",
                "You are given the heads of two sorted linked lists list1 and list2. Merge the two lists into one sorted list. The list should be made by splicing together the nodes of the first two lists. Return the head of the merged linked list.",
                Problem.Difficulty.EASY,
                "linked-list,recursion",
                "list1 = [1,2,4], list2 = [1,3,4]",
                "[1,1,2,3,4,4]",
                "The number of nodes in both lists is in the range [0, 50]\n-100 <= Node.val <= 100",
                60.5, 6300
            ),
            createProblem(
                "Maximum Subarray",
                "Given an integer array nums, find the subarray with the largest sum, and return its sum.",
                Problem.Difficulty.MEDIUM,
                "array,dp,divide-and-conquer",
                "nums = [-2,1,-3,4,-1,2,1,-5,4]",
                "6",
                "1 <= nums.length <= 10^5\n-10^4 <= nums[i] <= 10^4",
                49.6, 7850
            ),
            createProblem(
                "Binary Tree Level Order Traversal",
                "Given the root of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level).",
                Problem.Difficulty.MEDIUM,
                "tree,bfs,binary-tree",
                "root = [3,9,20,null,null,15,7]",
                "[[3],[9,20],[15,7]]",
                "The number of nodes in the tree is in the range [0, 2000]\n-1000 <= Node.val <= 1000",
                65.0, 4500
            ),
            createProblem(
                "Coin Change",
                "You are given an integer array coins representing coins of different denominations and an integer amount representing a total amount of money. Return the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return -1.",
                Problem.Difficulty.MEDIUM,
                "array,dp,bfs",
                "coins = [1,5,11], amount = 15",
                "3",
                "1 <= coins.length <= 12\n1 <= coins[i] <= 2^31 - 1\n0 <= amount <= 10^4",
                41.2, 8100
            ),
            createProblem(
                "Trapping Rain Water",
                "Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.",
                Problem.Difficulty.HARD,
                "array,two-pointers,dp,stack,monotonic-stack",
                "height = [0,1,0,2,1,0,1,3,2,1,2,1]",
                "6",
                "n == height.length\n1 <= n <= 2 * 10^4\n0 <= height[i] <= 10^5",
                58.3, 6600
            ),
            createProblem(
                "Median of Two Sorted Arrays",
                "Given two sorted arrays nums1 and nums2 of size m and n respectively, return the median of the two sorted arrays. The overall run time complexity should be O(log (m+n)).",
                Problem.Difficulty.HARD,
                "array,binary-search,divide-and-conquer",
                "nums1 = [1,3], nums2 = [2]",
                "2.0",
                "nums1.length == m\nnums2.length == n\n0 <= m <= 1000\n0 <= n <= 1000\n1 <= m + n <= 2000",
                36.1, 9200
            )
        );
        problemRepository.saveAll(problems);
        log.info("Seeded {} problems.", problems.size());
    }

    private void seedSubmissions() {
        // Retrieve saved users and problems
        List<User>    users    = userRepository.findAll();
        List<Problem> problems = problemRepository.findAll();

        if (users.isEmpty() || problems.isEmpty()) {
            log.warn("Cannot seed submissions - users or problems table is empty.");
            return;
        }

        // Build 20 representative submissions
        List<Submission> submissions = List.of(
            createSubmission(users.get(0).getId(), problems.get(0).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,     120),
            createSubmission(users.get(0).getId(), problems.get(1).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,     95),
            createSubmission(users.get(1).getId(), problems.get(0).getId(), "python", samplePythonCode(), Submission.Status.ACCEPTED,   88),
            createSubmission(users.get(1).getId(), problems.get(3).getId(), "python", samplePythonCode(), Submission.Status.WRONG_ANSWER, 120),
            createSubmission(users.get(1).getId(), problems.get(3).getId(), "python", samplePythonCode(), Submission.Status.ACCEPTED,   105),
            createSubmission(users.get(2).getId(), problems.get(0).getId(), "java",   sampleJavaCode(), Submission.Status.COMPILE_ERROR, 0),
            createSubmission(users.get(2).getId(), problems.get(0).getId(), "java",   sampleJavaCode(), Submission.Status.WRONG_ANSWER, 200),
            createSubmission(users.get(2).getId(), problems.get(4).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,   110),
            createSubmission(users.get(3).getId(), problems.get(2).getId(), "javascript", sampleJsCode(), Submission.Status.ACCEPTED, 75),
            createSubmission(users.get(3).getId(), problems.get(5).getId(), "javascript", sampleJsCode(), Submission.Status.TIME_LIMIT, 2100),
            createSubmission(users.get(3).getId(), problems.get(5).getId(), "javascript", sampleJsCode(), Submission.Status.WRONG_ANSWER, 350),
            createSubmission(users.get(4).getId(), problems.get(8).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,   180),
            createSubmission(users.get(4).getId(), problems.get(9).getId(), "java",   sampleJavaCode(), Submission.Status.TIME_LIMIT, 2000),
            createSubmission(users.get(4).getId(), problems.get(9).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,   220),
            createSubmission(users.get(0).getId(), problems.get(6).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,   140),
            createSubmission(users.get(1).getId(), problems.get(7).getId(), "python", samplePythonCode(), Submission.Status.WRONG_ANSWER, 180),
            createSubmission(users.get(1).getId(), problems.get(7).getId(), "python", samplePythonCode(), Submission.Status.ACCEPTED,   155),
            createSubmission(users.get(2).getId(), problems.get(1).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,   90),
            createSubmission(users.get(3).getId(), problems.get(3).getId(), "javascript", sampleJsCode(), Submission.Status.COMPILE_ERROR, 0),
            createSubmission(users.get(4).getId(), problems.get(2).getId(), "java",   sampleJavaCode(), Submission.Status.ACCEPTED,   60)
        );
        submissionRepository.saveAll(submissions);

        // Update acceptance rates for each problem after seeding
        for (Problem p : problems) {
            long total    = submissionRepository.countByProblemId(p.getId());
            long accepted = submissionRepository.countByProblemIdAndStatus(p.getId(), Submission.Status.ACCEPTED);
            if (total > 0) {
                p.setTotalSubmissions((int) total);
                p.setAcceptanceRate(Math.round((accepted * 100.0 / total) * 10.0) / 10.0);
                problemRepository.save(p);
            }
        }

        log.info("Seeded {} submissions.", submissions.size());
    }

    // ----------------------------------------------------------------
    //  Factory helpers
    // ----------------------------------------------------------------

    private User createUser(String username, String email, String password,
                            User.Role role, int totalSolved, String rank) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(sha256(password));
        u.setRole(role);
        u.setTotalSolved(totalSolved);
        u.setRank(rank);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    private Problem createProblem(String title, String description, Problem.Difficulty difficulty,
                                  String tags, String exIn, String exOut,
                                  String constraints, double acceptance, int total) {
        Problem p = new Problem();
        p.setTitle(title);
        p.setDescription(description);
        p.setDifficulty(difficulty);
        p.setTags(tags);
        p.setExampleInput(exIn);
        p.setExampleOutput(exOut);
        p.setConstraints(constraints);
        p.setAcceptanceRate(acceptance);
        p.setTotalSubmissions(total);
        return p;
    }

    private Submission createSubmission(Long userId, Long problemId, String language,
                                        String code, Submission.Status status, int execTime) {
        Submission s = new Submission();
        s.setUserId(userId);
        s.setProblemId(problemId);
        s.setLanguage(language);
        s.setCode(code);
        s.setStatus(status);
        s.setExecutionTimeMs(execTime);
        s.setSubmittedAt(LocalDateTime.now().minusMinutes((long)(Math.random() * 10000)));
        return s;
    }

    // ----------------------------------------------------------------
    //  Sample code snippets (for realistic demo data)
    // ----------------------------------------------------------------

    private String sampleJavaCode() {
        return "class Solution {\n" +
               "    public int[] twoSum(int[] nums, int target) {\n" +
               "        Map<Integer, Integer> map = new HashMap<>();\n" +
               "        for (int i = 0; i < nums.length; i++) {\n" +
               "            int complement = target - nums[i];\n" +
               "            if (map.containsKey(complement)) {\n" +
               "                return new int[]{ map.get(complement), i };\n" +
               "            }\n" +
               "            map.put(nums[i], i);\n" +
               "        }\n" +
               "        return new int[]{};\n" +
               "    }\n" +
               "}";
    }

    private String samplePythonCode() {
        return "class Solution:\n" +
               "    def twoSum(self, nums: List[int], target: int) -> List[int]:\n" +
               "        seen = {}\n" +
               "        for i, num in enumerate(nums):\n" +
               "            complement = target - num\n" +
               "            if complement in seen:\n" +
               "                return [seen[complement], i]\n" +
               "            seen[num] = i\n" +
               "        return []";
    }

    private String sampleJsCode() {
        return "/**\n * @param {number[]} nums\n * @param {number} target\n * @return {number[]}\n */\n" +
               "var twoSum = function(nums, target) {\n" +
               "    const map = new Map();\n" +
               "    for (let i = 0; i < nums.length; i++) {\n" +
               "        const complement = target - nums[i];\n" +
               "        if (map.has(complement)) return [map.get(complement), i];\n" +
               "        map.set(nums[i], i);\n" +
               "    }\n" +
               "};";
    }

    // ----------------------------------------------------------------
    //  SHA-256 helper (mirrors UserService.hashPassword)
    // ----------------------------------------------------------------

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
