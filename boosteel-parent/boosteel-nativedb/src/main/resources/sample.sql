
# select.work
SELECT this.*, customer.*
            FROM hancomee_work this
            INNER JOIN hancomee_customer customer ON this.customer_id = customer.id
            LEFT OUTER JOIN hancomee_workitem item ON this.id = item.work_id
            [INNER JOIN hancomee_workmemo memo ON work.id = memo.work_id  |memo]
            [INNER JOIN hancomee_workfile_ref ref ON ref.work_id = this.id  |ref]
            [INNER JOIN hancomee_workfile wfile1 ON ref.id = wfile1.id  |ref]
            [INNER JOIN hancomee_workfile_print print ON print.item_id = item.id  |print]
            [INNER JOIN hancomee_workfile wfile3 ON print.id = wfile3.id  |print]
            WHERE this.state = :state{i}
            [AND this.title LIKE :%title%]
            [AND wfile3.original_name LIKE :%print%]
            [AND wfile1.original_name LIKE :%ref%]
            [AND customer.name LIKE :%customerName%]
            [AND item.subject LIKE :%itemSubject%]
            [AND memo.value LIKE :%memo%]
            [AND this.datetime BETWEEN :st{st} AND :et{et}]
            GROUP BY this.id;

# state.work
SELECT r.state idx, count(r.state) count, sum(r.total) sum
FROM
	(
		SELECT work.state, work.total FROM hancomee_work work
            [INNER JOIN hancomee_customer customer ON work.customer_id = customer.id   |customerName]
            [INNER JOIN hancomee_workitem item ON work.id = item.work_id   |itemSubject,print]
            [INNER JOIN hancomee_workmemo memo ON work.id = memo.work_id   |memo]
            [INNER JOIN hancomee_workfile_ref ref ON ref.work_id = work.id  |ref]
            [INNER JOIN hancomee_workfile wfile1 ON ref.id = wfile1.id  |ref]
            [INNER JOIN hancomee_workfile_print print ON print.item_id = item.id  |print]
            [INNER JOIN hancomee_workfile wfile3 ON print.id = wfile3.id  |print]
            WHERE 1
            [AND work.title LIKE :%title%]
            [AND wfile3.original_name LIKE :%print%]
            [AND wfile1.original_name LIKE :%ref%]
            [AND customer.name LIKE :%customerName%]
            [AND item.subject LIKE :%itemSubject%]
            [AND memo.value LIKE :%memo%]
            [AND work.datetime BETWEEN :st{st} AND :et{et}]
            GROUP BY work.id
	) r
GROUP BY r.state;



/*

 GROUP BY work.id 를 쓰게 되면 행이 나뉘게 된다.
 GROUP BY는 아마도 결과를 행으로 그룹화시키는 역할을 하는 것 같다.

 */
# count.work
SELECT count(DISTINCT work.id)
            FROM hancomee_work work
            [INNER JOIN hancomee_customer customer ON work.customer_id = customer.id   |customerName]
            [INNER JOIN hancomee_workitem item ON work.id = item.work_id   |itemSubject,print]
            [INNER JOIN hancomee_workmemo memo ON work.id = memo.work_id  |memo]
            [INNER JOIN hancomee_workfile_ref ref ON ref.work_id = work.id  |ref]
            [INNER JOIN hancomee_workfile wfile1 ON ref.id = wfile1.id  |ref]
            [INNER JOIN hancomee_workfile_print print ON print.item_id = item.id  |print]
            [INNER JOIN hancomee_workfile wfile3 ON print.id = wfile3.id  |print]
            WHERE work.state = :state{i}
            [AND work.title LIKE :%title%]
            [AND wfile3.original_name LIKE :%print%]
            [AND wfile1.original_name LIKE :%ref%]
            [AND customer.name LIKE :%customerName%]
            [AND item.subject LIKE :%itemSubject%]
            [AND memo.value LIKE :%memo%]
            [AND work.activetime BETWEEN :st{ast} AND :et{aet}]
            [AND work.datetime BETWEEN :st{st} AND :et{et}];



# select.receivable
SELECT this.* FROM receivable this ORDER BY this.date DESC;


# report.work
SELECT * FROM hancomee_work this
				INNER JOIN hancomee_customer customer ON this.customer_id = customer.id
        INNER JOIN hancomee_workitem items ON this.id = items.work_id
			  WHERE this.state = 6 AND this.activetime BETWEEN :st{st} AND :et{et};


# select.draft
SELECT this.save_name, this.filetype
  FROM hancomee_workitem item
  INNER JOIN hancomee_workfile_draft draft  ON item.id = draft.item_id
  INNER JOIN hancomee_workfile this ON draft.id = this.id
  WHERE item.work_id = :workId{i} ORDER BY this.datetime DESC LIMIT 0, 1;
