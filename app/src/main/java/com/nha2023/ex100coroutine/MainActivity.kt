package com.nha2023.ex100coroutine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout.DispatchChangeEvent
import com.nha2023.ex100coroutine.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var job : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Cotoutine[코루틴] - 경량스레드 : 스레드를 멈추지않고 비동기 처리 - 하나의 스레드안에 여러개의 코루틴 설계
        //스레드가 요리사라면 멀티스레드는 여러 요리사가 화구(cpu)를 번걸아 사용하는 기술. 다른 요리사가 사용중에는 기존 요리사는 동작을 멈춤
        //코루틴은 하나의 요리사(스레드)가 파스타를 만들면서 스테이크까지 굽는 형식. 즉, 팬이 2개인 것임. 자리를 비켜가면서 멈추는 행동이 없어서 속도가 좀 더 빠르다.

        //코루틴을 구동하는 2개의 범위(scope)가 존재함.
        //첫번째. GlobalScope : 앱 전체의 생명주기와 함께 관리되는 범위 (앱이 끝날때까지 계속 작업한다. 사용성이 적다. 이런 작업은 거의 안함)
        //두번째. CoroutineScope : 버튼 클릭같은 특정 이벤트 순간에 해야할 Job을 위해 실행되는 범위
        //[network통신, DB CRUD, 특정 반복문 or 연산 등등]

        //실습 1) GlobalScope 코드 연습
        binding.btn.setOnClickListener {
            //연습삼아 코루틴 없이 오래걸리는 작업 실행해보기
//            for(n in 0..9){
//                Log.d("TAG","n:$n")
//                Thread.sleep(500) //잠재우기
//            }

        //실습 2)비동기 작업으로 위 작업을 수행 - 코루틴 사용해보기
        GlobalScope.launch {
                for(n in 0..9){
                    Log.d("TAG","n:$n - ${Thread.currentThread().name}") //Thread.currentThread().name 스레드 이름이 누구니?
                    //이걸 메인스레드에게 던지지않는다 누구에게 던질까? DefaultDispatcher-worker-1
                    //Thread.sleep(500) 는 요리사를 실제로 잠재움
                    delay(500)
                }
        } //이 for문과 아래 토스트는 동시에 작업된다. 토스트+숫자세는것이 동시에 됨. 경량스레드 역할

            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show()
        }

        //실습 3)비동기 작업으로 Corutine 실행
        //CorutineScope는 GlobalScope와 다르게 해당 작업을 어떤 스레드에게 보낼지 결정하는 Dispatcher[디스패처]를 지정해야 함.
        //보통 1,2,3을 많이 쓰고, IO를 많이쓰게될것이다.
        //Dispatcher의 종류
        //(1) Dispatchers.Defalut    - 기본 스레드풀의 스레드를 사용 [cpu작업이 많은 연산작업에 적합]
        //(2) Dispatchers.IO         - DB나 네트워크 IO 스레드를 사용 [파일입출력, 서버작업에 적합]
        //(3) Dispatchers.Main       - MainThread를 사용한다. [ UI작업 등에 적합 ]
        //(4) Dispatchers.Unconfined - 조금 특별한 디스패처 [해당 코루틴을 호출하는 스레드에서 실행]
        binding.btnCs.setOnClickListener {

            //(1) Dispatchers.Defalut 사용해보기 -> 원래 이거 되면 안되는데 된다...
            CoroutineScope(Dispatchers.Default).launch {
                //context는 저장소
                for(n in 0..9){
                    Log.d("TAG","n: $n - ${Thread.currentThread().name}")
                    binding.tv.text = "n : $n"
                    //작업을 한 스레드의 이름 : DefaultDispatcher-worker-1
                    delay(500)
                }
            }
            Toast.makeText(this, "hi~~", Toast.LENGTH_SHORT).show()
        }

        //실습 4)
        binding.btnMain.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                for (n in 0..9) {
                    Log.d("TAG", "n: $n - ${Thread.currentThread().name}")
                    binding.tv.text = "n : $n"
                    //작업을 한 스레드의 이름 : DefaultDispatcher-worker-1
                    delay(500)
                }
            }
            Toast.makeText(this, "hi~~", Toast.LENGTH_SHORT).show()
            //하나의 스레드가 코루틴 여러개를 할수있다.
        }

        //실습 5)
        binding.btnServerMain.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                //네트워크 이미지 불러오기 -->에러! 메인스레드는 네트워크 작업 불가능

                val url = URL("https://cdn.pixabay.com/photo/2023/06/01/06/22/british-shorthair-8032816_640.jpg")
                val bm:Bitmap = BitmapFactory.decodeStream(url.openStream())

                binding.iv.setImageBitmap(bm)
            }
        }

        //실습 6)
        binding.btnIo.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val url = URL("https://cdn.pixabay.com/photo/2023/06/01/06/22/british-shorthair-8032816_640.jpg")
                val bm:Bitmap = BitmapFactory.decodeStream(url.openStream())
                //에러!! 바로꺼짐..! 입출력 전용 스레드는 UI 변경 불가
                //밑에 처럼 메인과 같이 가는 함수를 써줘야한다.
                withContext(Dispatchers.Main){
                    binding.iv.setImageBitmap(bm)
                }


            }
        }

        //실습 7)
        binding.btn6.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                //작업 1
                launch {
                    for(n1 in 1000..1010){
                        Log.d("TAG","n1:$n1")
                        delay(500)
                    }
                }

                //작업 2
                launch {
                    for(n2 in 1000..1010){
                        Log.d("TAG","n2:$n2")
                        delay(500)
                    }
                }
            }

        }//btn6

        //실습 8) join을 하게되면 순차적으로 출력된다.
        binding.btn7.setOnClickListener {

            CoroutineScope(Dispatchers.Default).launch {
                //작업 1
                launch {
                    for(n1 in 1000..1010){
                        Log.d("TAG","n1:$n1")
                        delay(500)
                    }
                }.join()

                //작업 2
                launch {
                    for(n2 in 1000..1010){
                        Log.d("TAG","n2:$n2")
                        delay(500)
                    }
                }.join()
            }

        }
        binding.btn8.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                someTask()
            }
        }

        binding.btn9.setOnClickListener {
            //스코프를 런치하면 참조할수있다. 상단 멤버변수에 job을 만들어준다.
           job = CoroutineScope(Dispatchers.Default).launch {
                for (n in 300..310){
                    Log.d("TAG","n:$n")
                    delay(500)
                }
            }
        }

        binding.btn10.setOnClickListener {
           job?.cancel()
                }
        //근데 이건 내가 일일히 작업하는것, 안드로이드 생명주기를 알아서 보고 처리하는 녀석
        }

    }//onCreate

    suspend fun someTask(){
        //코루틴 스코프 범위 밖에서 코루틴의 기능을 사용할 때 함수를 suspend 함수로 만들면 해결
        for(n in 1000..1010){
            Log.d("TAG","someTask : $n")
            delay(500) //딜레이는 코루틴안에서만 써야한다. 특정 메소드에서 코루틴의 기능을 못쓰게된다.. 이때 쓸수있는!!! suspend

        }


}//MainActivity
