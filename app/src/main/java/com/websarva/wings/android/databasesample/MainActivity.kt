package com.websarva.wings.android.databasesample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {
    // 選択されたカクテルの主キーIDを表すプロパティ
    private var _cooktailld = -1
    // 選択されたカクテル名を表すプロパティ
    private var _cooktailName = ""
    // データベースヘルパーオブジェクト
    private val _helper = DatabaseHelper(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // カクテルリスト用ListViewを取得
        val lvCooktail = findViewById<ListView>(R.id.lvCooktail)
        lvCooktail.onItemClickListener = ListItemClickListener()
    }

    override fun onDestroy() {
        // ヘルパーオブジェクトの開放
        _helper.close()
        super.onDestroy()
    }

    // 保存ボタンがタップされた時の処理メソッド
    fun onSaveButtonClick(view: View) {
        // 感想欄を取得
        val etNote = findViewById<EditText>(R.id.etNote)
        // 入力された感想を取得
        val note = etNote.text.toString()

        // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
        val db = _helper.writableDatabase

        // ますリストで選択されたカクテルのメモデータを削除、その後インサート
        val sqlDelete = "DELETE FROM cooktailmemos WHERE _id = ?"
        // SQL文字列をもとにプリペアドステートメントを取得
        var stmt = db.compileStatement(sqlDelete)
        // 変数のバインド
        stmt.bindLong(1, _cooktailld.toLong())
        stmt.executeUpdateDelete()

        // インサート用SQL
        val sqlInsert = "Insert INTO cooktailmemos(_id, name, note) values(?, ?, ?)"
        stmt = db.compileStatement(sqlInsert)
        // 変数のバインド
        stmt.bindLong(1, _cooktailld.toLong())
        stmt.bindString(2, _cooktailName)
        stmt.bindString(3, note)
        stmt.executeInsert()

        // 感想欄の入力値を消去
        etNote.setText("")
        // カクテル名を表示するTextViewを取得
        val tvCooktailName = findViewById<TextView>(R.id.tvCooktailName)
        // カクテル名を未選択に変更
        tvCooktailName.text = getString(R.string.tv_name)
        // 保存ボタンを取得
        val btnSave = findViewById<Button>(R.id.btnSave)
        // 保存ボタンをタップできないよう変更
        btnSave.isEnabled = false
    }

    // リストがタップされた時の処理が記述されたメンバクラス
    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // タップされた行番号をプロパティの主キーIDに代入
            _cooktailld = position
            // タップされた行のデータを取得
            _cooktailName = parent.getItemAtPosition(position) as String
            // カクテル名を表示するTextViewを取得
            val tvCooktailName = findViewById<TextView>(R.id.tvCooktailName)
            //カクテル名を表示するTextViewに表示カクテル名を設定
            tvCooktailName.text = _cooktailName
            // 保存ボタンを取得
            val btnSave = findViewById<Button>(R.id.btnSave)
            // 保存ボタンをタップできるよう設定
            btnSave.isEnabled = true

            val db = _helper.writableDatabase
            val sql = "SELECT * FROM cooktailmemos WHERE _id = ${_cooktailld}"
            // SQLの実行
            val cursor = db.rawQuery(sql, null)
            var note = ""
            // SQL実行の戻り値であるカーソルオブジェクトをループさせデータを取得
            while(cursor.moveToNext()) {
                val idxNote = cursor.getColumnIndex("note")
                note = cursor.getString(idxNote)
            }
            val etNote = findViewById<EditText>(R.id.etNote)
            etNote.setText(note)
        }
    }
}