<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%
    String ctx = request.getContextPath();
%>
<!doctype html>
<html lang="sr">
<head>
    <meta charset="utf-8" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>EventBooker</title>

    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans:wght@400;600;700&display=swap" rel="stylesheet">

<%--    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">--%>

    <style>
        :root{
            --brand:#69247C; --accent:#DA498D; --highlight:#FAC67A; --bg:#F9E6CF;
            --text:#2B1831; --muted:#6E4C7A; --card:#FFFFFF; --border:#E9DAD0;
            --text-inv:#ffffff;
            --sidebar-w: 340px;
        }
        *{ box-sizing:border-box }
        body{
            margin:0; background:var(--bg); color:var(--text);
            font-family:"Noto Sans", ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, Ubuntu, Cantarell, Arial;
        }

        a{ color:inherit; text-decoration:none }
        .container{ max-width:1100px; margin:0 auto; padding:16px }


        .page{ }
        @media (min-width: 1100px){
            .page{ margin-right: var(--sidebar-w); }
        }


        .nav{
            position:sticky; top:0; z-index:20; background:var(--card);
            border-bottom:1px solid var(--border); width:100%;
        }
        .nav-inner{ display:flex; align-items:center; gap:10px; padding:10px 16px; }
        .nav-left{ display:flex; gap:8px; align-items:center; flex-wrap:wrap }
        .nav-right{ margin-left:auto; display:flex; gap:10px; align-items:center }
        .profile-pill{ display:inline-flex; align-items:center; gap:8px; border:1px solid var(--border); border-radius:999px; padding:6px 10px; background:#fff }
        .role-chip{ display:inline-flex; min-width:34px; justify-content:center; align-items:center;
            height:22px; border-radius:999px; padding:0 8px; font-size:12px; color:var(--text-inv); background:var(--brand); }

        .btn, button{ cursor:pointer; border:1px solid var(--border); background:var(--card);
            color:var(--text); padding:8px 12px; border-radius:10px; font-weight:600 }
        .btn:hover{ filter:brightness(1.03) }
        .btn.brand{ background:var(--brand); border-color:var(--brand); color:var(--text-inv) }
        .btn.accent{ background:var(--accent); border-color:var(--accent); color:var(--text-inv) }
        .btn.ghost{ background:transparent; border-color:transparent; color:var(--brand) }

        .field{ display:flex; align-items:center; gap:8px; background:#fff; border:1px solid var(--border); border-radius:10px; padding:0 10px; }
        .field input{ border:none; outline:none; padding:8px 4px; min-width:240px; font:inherit; color:inherit; background:transparent }


        .dropdown{ position:relative }
        .dropdown-menu{
            position:absolute; left:0; top:calc(100% + 8px); min-width:260px; max-height:360px;
            overflow:auto; background:var(--card); border:1px solid var(--border); border-radius:12px;
            box-shadow:0 8px 30px rgba(0,0,0,.08); padding:8px; display:none;
        }
        .dropdown.open .dropdown-menu{ display:block }
        .dropdown-menu a{ display:block; padding:8px 10px; border-radius:8px; color:var(--text) }
        .dropdown-menu a:hover{ background:rgba(105,36,124,.08); color:var(--brand) }


        .grid{ display:grid; gap:16px }


        .grid.cards{
            grid-template-columns: 1fr;
        }
        @media (min-width: 900px){
            .grid.cards{ grid-template-columns: repeat(2, minmax(0,1fr)); }
        }

        .card{
            background:var(--card); border:1px solid var(--border); border-radius:16px; padding:16px;
            transition: transform .08s ease, box-shadow .08s ease;
        }
        .card-link{ display:block }
        .card.card-link:hover{
            transform: translateY(-2px);
            box-shadow:0 8px 24px rgba(0,0,0,.08);
        }

        .row{ display:flex; gap:12px; align-items:center }
        .row.wrap{ flex-wrap:wrap }
        .badge{ display:inline-block; padding:3px 8px; border-radius:999px; background:#FFF6E8; color:#8A5B00; border:1px solid #F5DEB3; font-size:12px }
        .muted{ color:var(--muted) }
        .pill{ border:1px solid var(--border); padding:2px 8px; border-radius:999px; font-size:12px; color:var(--muted) }


        .kpi{
            display:flex; gap:6px; align-items:center;
            background:#FFF7FB; border:1px solid var(--border);
            padding:4px 8px; border-radius:10px; font-size:12px;
        }
        .kpi .num{ font-weight:700; color:var(--brand) }

        .table{ width:100%; border-collapse:collapse; font-size:14px }
        .table th, .table td{ border-bottom:1px solid var(--border); padding:10px; text-align:left }
        .table th{ color:var(--muted); font-weight:600 }

        .pagination{ display:flex; gap:4px; justify-content:center }
        .pagination button{ padding:6px 10px }


        .most-reacted-card{
            position:fixed; right:16px; top:76px; z-index:40;
            display:inline-block;
            inline-size: max-content;
            max-width: 360px;
            white-space: nowrap;
            overflow: hidden;
            background:var(--card); border:1px solid var(--border); border-radius:12px; padding:12px;
            box-shadow:0 12px 40px rgba(105,36,124,.12);
        }
        .most-reacted-card h4{ margin:0 0 8px; font-size:14px; color:var(--brand) }
        .most-reacted-card ul{ list-style:none; margin:0; padding:0; display:grid; gap:8px }
        .most-reacted-card a:hover{ color:var(--accent) }
        @media (max-width: 1100px){
            .most-reacted-card{ display:none }
        }


        .category-chip{
            margin-left:auto;
            background:#F1ECF7; color:#5a2a73;
            border:1px solid #e5d9f2; border-radius:999px;
            padding:4px 10px; font-size:12px; font-weight:600;
        }
    </style>


    <script crossorigin src="https://unpkg.com/react@18/umd/react.development.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
</head>
<body>
<div id="root"></div>

<script type="text/babel" data-presets="env,react">
    const { useEffect, useState } = React;


    const API = {
        async req(path, opts={}) {
            const res = await fetch(path, {
                credentials: 'include',
                headers: { 'Content-Type': 'application/json', ...(opts.headers||{}) },
                ...opts
            });
            const ct = res.headers.get('content-type') || '';
            const data = ct.includes('application/json') ? await res.json().catch(()=>null) : await res.text();
            if (!res.ok) throw { status:res.status, data };
            return data;
        },
        get(p){ return this.req(p) },
        post(p, body){ return this.req(p, { method:'POST', body: JSON.stringify(body||{}) }) },
        put(p, body){ return this.req(p, { method:'PUT', body: JSON.stringify(body||{}) }) },
        del(p, body){ return this.req(p, { method:'DELETE', body: body?JSON.stringify(body):undefined }) },
    };

    const fmtDate = (s) => s ? new Date(s).toLocaleString() : '';
    const roleAbbrev = (t) => {
        const x = (t||'').toUpperCase();
        if (x === 'ADMIN') return 'Admin';
        if (x === 'CREATOR' || x === 'EVENT_CREATOR') return 'EC';
        return x || '—';
    };


    function normalizeUser(u){
        if (!u) return null;
        const firstName = (u.firstName ?? u.first_name ?? '').toString();
        const lastName  = (u.lastName  ?? u.last_name  ?? '').toString();
        const rawType = (
             u.type?? u.role ?? ''
        ).toString().toUpperCase();

        let userType = '';
        if (rawType.includes('ADMIN')) userType = 'ADMIN';
        else if (rawType.includes('CREATOR')) userType = 'EVENT_CREATOR';

        return { ...u, firstName, lastName, userType };
    }

    function roleLabel(u){
        return u?.userType === 'ADMIN' ? 'Admin'
            : u?.userType === 'EVENT_CREATOR' ? 'EC'
                : '';
    }

    function displayName(u){
        const full = ((u?.firstName||'') + ' ' + (u?.lastName||'')).trim();
        return full || (u?.email || '');
    }


    function MostReacted({ limit=3 }){
        const [items, setItems] = useState([]);
        useEffect(()=>{ API.get('/api/public/events/most-reacted?limit='+limit).then(r=> setItems(r.items||r||[])).catch(()=>setItems([])); }, [limit]);
        return (
            <aside className="most-reacted-card" aria-label="Najviše reakcija">
                <h4>Najviše reakcija</h4>
                <ul>
                    {items.map(e => <li key={e.id}><a href={'#/event/'+e.id}>{e.title}</a></li>)}
                </ul>
            </aside>
        );
    }


    const getAuthor = (e) => (e.authorName||e.author_name) ||
        ((e.authorFirstName||e.author_first_name)||'') + ' ' + ((e.authorLastName||e.author_last_name)||'');
    const getCategoryName = (e) => e.categoryName || e.category_name || e.category?.name || '';


    function EventCard({ e }){
        const { useState, useEffect } = React;


        const views    = e.views ?? e.viewsCount ?? e.views_count ?? 0;
        const likes    = e.likes ?? 0;
        const dislikes = e.dislikes ??  0;


        const author = (getAuthor(e) || '').trim();


        const initialCatName = getCategoryName(e);
        const catId = e.categoryId ?? e.category_id ?? e.category?.id ?? null;

        const [catLabel, setCatLabel] = useState(initialCatName);


        if (!window.__catById) window.__catById = {};
        const catById = window.__catById;

        useEffect(() => {
            setCatLabel(initialCatName);
        }, [initialCatName, e?.id]);

        useEffect(() => {
            let abort = false;
            (async ()=>{
                if (!catLabel && catId) {
                    if (catById[catId]) {
                        if (!abort) setCatLabel(catById[catId]);
                        return;
                    }
                    try{
                        const all = await API.get('/api/public/categories').catch(()=>[]);
                        const arr = (all?.items) || all || [];
                        arr.forEach(c => { catById[c.id] = c.name; });
                        if (!abort) setCatLabel(catById[catId] || '');
                    }catch(_){}
                }
            })();
            return ()=>{ abort = true };
        }, [catLabel, catId]);

        const goCat = (ev)=>{
            if (!catId) return;
            ev.preventDefault(); ev.stopPropagation();
            window.location.hash = `#/events?categoryId=${encodeURIComponent(catId)}&page=1`;
        };

        return (
            <a className="card card-link" aria-label={e.title} href={'#/event/'+e.id}>
                <div className="row" style={{justifyContent:'space-between'}}>
                    <h3 style={{margin:'0 0 6px 0'}}>{e.title}</h3>
                    <span className="badge">{fmtDate(e.scheduledAt||e.scheduled_at)}</span>
                </div>

                {author && <p className="muted" style={{margin:'2px 0 6px 0'}}>by {author}</p>}

                {e.location && (
                    <p className="muted" style={{margin:'0 0 10px 0'}}>
                        <strong>Lokacija:</strong> {e.location}
                    </p>
                )}

                <p
                    className="muted"
                    style={{
                        margin:'0 0 10px 0',
                        overflow:'hidden',
                        display:'-webkit-box',
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: 'vertical',
                        wordBreak: 'break-word',
                        overflowWrap: 'anywhere'
                    }}
                    title={e.description || ''}
                >
                    {e.description || ''}
                </p>

                <div className="row wrap" style={{margin:'8px 0', gap:8}}>
                    {catLabel && (
                        <span
                            role="link"
                            tabIndex={0}
                            className="category-chip"
                            onClick={goCat}
                            onKeyDown={(ev)=>{ if(ev.key==='Enter'||ev.key===' ') goCat(ev); }}
                            title="Vidi događaje u ovoj kategoriji"
                        >
            {catLabel}
          </span>
                    )}
                    <span className="kpi"><span className="num">{views}</span> pregleda</span>
                    <span className="kpi"><span className="num">{likes}</span> 👍</span>
                    <span className="kpi"><span className="num">{dislikes}</span> 👎</span>
                </div>
            </a>
        );
    }

  // PUBLIC
    function Home(){
        const [items, setItems] = React.useState([]);
        const [page, setPage]   = React.useState(1);
        const limit = 10;
        const [total, setTotal] = React.useState(0);

        const load = async ()=>{
            const r = await API.get(`/api/public/events?page=${page}&limit=${limit}`);
            setItems(r.items || r || []);
            setTotal(r.total ?? (r.items ? r.items.length : (r||[]).length));
        };
        React.useEffect(()=>{ load() }, [page]);

        const pages = Math.max(1, Math.ceil((total||0)/limit));
        const go = (p)=> setPage(Math.min(pages, Math.max(1, p)));

        return (
            <div className="container page grid" style={{gap:24}}>
                <h2 style={{margin:'8px 0'}}>Najnoviji</h2>
                <div className="grid cards">{items.map(e=> <EventCard key={e.id} e={e} />)}</div>

                {pages>1 && (
                    <div className="row" style={{justifyContent:'center'}}>
                        <div className="pagination">
                            <button onClick={()=>go(1)} disabled={page===1}>«</button>
                            <button onClick={()=>go(page-1)} disabled={page===1}>‹</button>
                            <span className="pill">Strana {page} / {pages}</span>
                            <button onClick={()=>go(page+1)} disabled={page===pages}>›</button>
                            <button onClick={()=>go(pages)} disabled={page===pages}>»</button>
                        </div>
                    </div>
                )}
            </div>
        );
    }



    function MostVisited(){
        const [items, setItems] = React.useState([]);
        const days  = 30;

        React.useEffect(() => {
            (async () => {
                const r = await API.get(`/api/public/events/most-visited?days=${days}`);
                setItems(Array.isArray(r) ? r : (r.items || []));
            })();
        }, []);

        return (
            <div className="container page grid" style={{gap:24}}>
                <h2 style={{margin:'8px 0'}}>Najposećeniji ({days} dana)</h2>
                <div className="grid cards">
                    {items.map(e => <EventCard key={e.id} e={e} />)}
                </div>
            </div>
        );
    }




    function EventsList({ route, push }){

        const parseFromRoute = (r) => {
            const qs = new URLSearchParams(r.split('?')[1] || '');
            return {
                q: qs.get('q') || '',
                page: parseInt(qs.get('page') || '1'),
                categoryId: qs.get('categoryId') || '',
                tag: qs.get('tag') || ''
            };
        };

        const init = parseFromRoute(route);

        const [q, setQ]         = React.useState(init.q);
        const [page, setPage]   = React.useState(init.page);
        const [limit, setLimit] = React.useState(12);
        const [items, setItems] = React.useState([]);
        const [total, setTotal] = React.useState(0);
        const [cat, setCat]     = React.useState(init.categoryId);
        const [tag, setTag]     = React.useState(init.tag);


        React.useEffect(() => {
            const p = parseFromRoute(route);
            setQ(p.q);
            setPage(p.page);
            setCat(p.categoryId);
            setTag(p.tag);
        }, [route]);

        const load = async ()=>{
            const base = q.trim() ? '/api/search' : '/api/public/events';
            const url  = new URL(base, window.location.origin);
            url.searchParams.set('page', page);
            url.searchParams.set('limit', limit);
            if (q.trim()) url.searchParams.set('q', q.trim());
            if (!q.trim()){
                if (tag) url.searchParams.set('tag', tag);
                else if (cat) url.searchParams.set('categoryId', cat);
            }
            const r = await API.get(url.toString());
            setItems(r.items || r || []);
            setTotal(r.total ?? (r.items ? r.items.length : (r||[]).length));
        };
        React.useEffect(() => { load() }, [q, page, limit, cat, tag]);

        const pages = Math.max(1, Math.ceil((total||0) / limit));
        const go = (p) => {
            const np = Math.min(pages, Math.max(1, p));
            setPage(np);

            push('/events?q=' + encodeURIComponent(q)
                + '&tag=' + encodeURIComponent(tag||'')
                + '&categoryId=' + (cat || '')
                + '&page=' + np);
        };

        return (
            <div className="container page grid" style={{gap:16}}>
                <div className="grid cards">{items.map(e=> <EventCard key={e.id} e={e} />)}</div>
                <div className="row" style={{justifyContent:'center'}}>
                    <div className="pagination">
                        <button onClick={()=>go(1)} disabled={page===1}>«</button>
                        <button onClick={()=>go(page-1)} disabled={page===1}>‹</button>
                        <span className="pill">Strana {page} / {pages}</span>
                        <button onClick={()=>go(page+1)} disabled={page===pages}>›</button>
                        <button onClick={()=>go(pages)} disabled={page===pages}>»</button>
                    </div>
                </div>
            </div>
        );
    }

    function Comments({ eventId }) {
        const [items, setItems]   = React.useState([]);
        const [total, setTotal]   = React.useState(0);
        const [page, setPage]     = React.useState(1);
        const [limit]             = React.useState(10);
        const [name, setName]     = React.useState("");
        const [text, setText]     = React.useState("");
        const [busy, setBusy]     = React.useState(false);
        const [err, setErr]       = React.useState(null);

        const loadCountsFor = async (arr) => {

            const withCounts = await Promise.all(arr.map(async c => {
                try {
                    const rc = await API.get(`/api/public/comments/${c.id}/reactions`);
                    return { ...c, likes: rc.likes ?? 0, dislikes: rc.dislikes ?? 0 };
                } catch (_) {
                    return { ...c, likes: 0, dislikes: 0 };
                }
            }));
            return withCounts;
        };

        const load = async () => {
            const url = new URL(`/api/public/events/${eventId}/comments`, window.location.origin);
            url.searchParams.set("page", page);
            url.searchParams.set("limit", limit);
            const r = await API.get(url.toString());
            const arr = r.items || r || [];
            setTotal(r.total ?? arr.length);
            setItems(await loadCountsFor(arr));
        };

        React.useEffect(() => { load(); }, [eventId, page, limit]);

        const submit = async (e) => {
            e.preventDefault();
            setErr(null);
            const payload = { name: name.trim(), text: text.trim() };
            if (!payload.name || !payload.text) { setErr("Unesite ime i tekst."); return; }
            if (payload.name.length > 100) { setErr("Ime je predugačko (max 100)."); return; }
            setBusy(true);
            try {
                await API.post(`/api/public/events/${eventId}/comments`, payload);
                setName(""); setText("");

                setPage(1);
                await load();
            } catch (ex) {
                const msg = ex?.data?.error === "name_and_text_required" ? "Ime i tekst su obavezni."
                    : ex?.data?.error === "name_too_long"           ? "Ime je predugačko."
                        : ex?.data?.message || "Greška pri slanju komentara.";
                setErr(msg);
            } finally {
                setBusy(false);
            }
        };

        const react = async (commentId, val) => {
            try {
                await API.post(`/api/public/comments/${commentId}/react`, { reaction: Number(val) });
                const rc = await API.get(`/api/public/comments/${commentId}/reactions`).catch(()=>({likes:0,dislikes:0}));
                setItems(lst => lst.map(c => c.id === commentId ? { ...c, likes: rc.likes||0, dislikes: rc.dislikes||0 } : c));
            } catch (_) {  }
        };

        const pages = Math.max(1, Math.ceil((total||0)/limit));
        const go = (p) => setPage(Math.min(pages, Math.max(1, p)));

        return (
            <div className="card">
                <h3>Komentari</h3>


                <form className="grid" style={{gap:8, marginBottom:12}} onSubmit={submit}>
                    <div className="row" style={{gap:8, flexWrap:'wrap'}}>
                        <label className="field" style={{maxWidth:260, flex: '1 1 220px'}}>
                            <input
                                placeholder="Vaše ime"
                                value={name}
                                onChange={e=>setName(e.target.value)}
                            />
                        </label>
                        <label className="field" style={{flex:'3 1 400px'}}>
                            <input
                                placeholder="Napišite komentar…"
                                value={text}
                                onChange={e=>setText(e.target.value)}
                            />
                        </label>
                        <button className="btn brand" disabled={busy}>{busy?'Slanje…':'Pošalji'}</button>
                    </div>
                    {err && <div className="card" style={{borderColor:'#f5c2c7', background:'#fff1f2', color:'#842029'}}>{err}</div>}
                </form>


                {items.length === 0 ? (
                    <p className="muted">Još nema komentara.</p>
                ) : (
                    <div className="grid" style={{gap:10}}>
                        {items.map(c => (
                            <div key={c.id} className="card" style={{padding:12}}>
                                <div className="row" style={{justifyContent:'space-between'}}>
                                    <strong>{c.name}</strong>
                                    <span className="muted">{fmtDate(c.createdAt||c.created_at)}</span>
                                </div>
                                <p style={{margin:'6px 0 10px 0'}}>{c.text}</p>
                                <div className="row" style={{gap:8}}>
                                    <button className="btn" onClick={()=>react(c.id, 1)}>👍 {c.likes ?? 0}</button>
                                    <button className="btn" onClick={()=>react(c.id, -1)}>👎 {c.dislikes ?? 0}</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}


                {pages>1 && (
                    <div className="row" style={{justifyContent:'center', marginTop:8}}>
                        <div className="pagination">
                            <button onClick={()=>go(1)} disabled={page===1}>«</button>
                            <button onClick={()=>go(page-1)} disabled={page===1}>‹</button>
                            <span className="pill">Strana {page} / {pages}</span>
                            <button onClick={()=>go(page+1)} disabled={page===pages}>›</button>
                            <button onClick={()=>go(pages)} disabled={page===pages}>»</button>
                        </div>
                    </div>
                )}
            </div>
        );
    }

    function EventDetails({ id }){
          const { useState, useEffect } = React;

          const [e,setE] = useState(null);
          const [related,setRelated] = useState([]);
          const [tags,setTags] = useState([]);
          const [categoryName,setCategoryName] = useState('');

          const [rsvpName,setRsvpName] = useState('');
          const [rsvpEmail,setRsvpEmail] = useState('');
          const [rsvpSeats,setRsvpSeats] = useState(1);
          const [rsvpCount,setRsvpCount] = useState(0);
          const [countSupported, setCountSupported] = useState(true);

          const load = async ()=>{
            const d = await API.get(`/api/public/events/${id}`);
            setE(d);


            const cn = getCategoryName(d) || '';
            setCategoryName(cn);
            if (!cn && (d.categoryId )) {
              const all = await API.get('/api/public/categories').catch(()=>[]);
              const arr = (all.items||all||[]);
              const cat = arr.find(c => String(c.id) === String(d.categoryId ));
              setCategoryName(cat?.name || '');
            }


            const tg = await API.get(`/api/public/tags/events/${id}`).catch(()=>[]);
            setTags((tg?.items)||tg||[]);


            const rel = await API.get(`/api/public/events/${id}/related?limit=3`).catch(()=>[]);
            setRelated((rel?.items)||rel||[]);


            if (countSupported){
              try{
                const rc = await API.get(`/api/public/events/${id}/bookings/count`);
                setRsvpCount(rc.count||0);
              }catch(_){ setCountSupported(false); }
            }
          };
          useEffect(()=>{ load() },[id]);

          if (!e) return <div className="container page"><p>Učitavanje…</p></div>;

          const views    = e.views ?? e.viewsCount ?? e.views_count ?? 0;
          const likes    = e.likes ?? 0;
          const dislikes = e.dislikes ?? 0;

          const createdAt   = fmtDate(e.createdAt || e.created_at);
          const scheduledAt = fmtDate(e.scheduledAt || e.scheduled_at);
          const author      = (getAuthor(e)||'').trim();
          const catId       = e.categoryId ?? e.category_id;
          const categoryHref= catId ? `#/events?categoryId=${encodeURIComponent(catId)}&page=1` : '#';

          const react = async (val)=>{
            await API.post(`/api/public/events/${id}/react`, { reaction:Number(val) });
            await load();
          };

          const rsvp  = async ()=>{
            try{
              await API.post(`/api/public/events/${id}/bookings`, {
                name:  rsvpName,
                email: rsvpEmail,
                seats: Number(rsvpSeats) || 1
              });
              await load();
              alert('Prijava zabeležena.');
            }catch(ex){ alert(ex?.data?.message || 'Greška pri prijavi'); }
          };

          const hasCapacity = typeof e.maxCapacity === 'number' && !Number.isNaN(e.maxCapacity);
          const capacityKpi = hasCapacity
            ? <span className="kpi">Mesta: <span className="num">{rsvpCount} / {e.maxCapacity}</span></span>
            : <span className="kpi">Prijavljeno: <span className="num">{rsvpCount}</span></span>;

          return (
            <div className="container page grid" style={{gap:16}}>
              <div className="card">

                <div className="row" style={{justifyContent:'space-between', alignItems:'flex-start', gap:8, flexWrap:'wrap'}}>
                  <h2 style={{margin:0}}>{e.title}</h2>
                  <span className="badge">Kreirano: {createdAt}</span>
                </div>


                {author && <p className="muted" style={{margin:'6px 0 10px 0'}}>by: {author}</p>}



                <p style={{ margin: '0 0 12px 0', overflowWrap: 'anywhere', wordBreak: 'break-word', whiteSpace: 'pre-wrap'}}> {e.description} </p>



                  <div className="grid" style={{gap: 4}}>
                      <p className="muted"><strong>Lokacija:</strong> {e.location}</p>
                      <p className="muted"><strong>Održava se:</strong> {scheduledAt}</p>
                </div>


                <div className="row wrap" style={{gap:8, marginTop:10}}>
                  {(categoryName || catId) ? (
                    <p className="pill" style={{width:'fit-content', margin:0}}>
                      <a href={categoryHref} title="Vidi događaje u ovoj kategoriji">
                        {categoryName || 'Kategorija'}
                      </a>
                    </p>
                  ) : null}
                  {tags.map(t => {
                    const nm = t.name || '';
                    return (
                      <a
                        key={t.id || nm}
                        href={`#/events?tag=${encodeURIComponent(nm)}&page=1`}
                        className="pill"
                        title={`Vidi događaje sa tagom #${nm}`}
                      >
                        #{nm}
                      </a>
                    );
                  })}
                </div>


                <div className="row wrap" style={{gap:10, marginTop:12}}>
                  <button className="btn brand" onClick={()=>react(1)}>👍 Sviđa mi se ({likes})</button>
                  <button className="btn" onClick={()=>react(-1)}>👎 Ne sviđa mi se ({dislikes})</button>
                  <span className="kpi"><span className="num">{views}</span> pregleda</span>
                  {capacityKpi}
                </div>
              </div>


              <Comments eventId={id} />

              <div className="card">
                <h3>RSVP</h3>
                <div className="row" style={{gap:8, flexWrap:'wrap'}}>
                  <input style={{maxWidth:'260px'}} placeholder="Vaše ime i prezime" value={rsvpName} onChange={e => setRsvpName(e.target.value)} />
                  <input style={{maxWidth:'320px'}} type="email" placeholder="Vaš email" value={rsvpEmail} onChange={e => setRsvpEmail(e.target.value)} />
                  <input style={{maxWidth:'140px'}} type="number" min="1" placeholder="Broj mesta" value={rsvpSeats} onChange={e => setRsvpSeats(e.target.value)} />
                  <button className="btn brand" onClick={rsvp}>Prijavi se</button>
                </div>
              </div>

              {(related && related.length>0) && (
                <div className="card">
                  <h3 style={{marginTop:0}}>Pročitaj još…</h3>
                  <div className="grid cards">{related.map(r => <EventCard key={r.id} e={r} />)}</div>
                </div>
              )}
            </div>
          );
        }


function CommentsSection({ eventId }){
  const { useState, useEffect } = React;
  const [items, setItems] = useState([]);
  const [name, setName] = useState('');
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);

  const load = async ()=>{
    const r = await API.get(`/api/public/events/${eventId}/comments?page=1&limit=50`).catch(()=>[]);
    setItems((r.items)||r||[]);
  };
  useEffect(()=>{ load() }, [eventId]);

  const submit = async (e)=>{
    e.preventDefault();
    if (!name.trim() || !text.trim()) { alert('Unesite ime i tekst.'); return; }
    setBusy(true);
    try{
      await API.post(`/api/public/events/${eventId}/comments`, { authorName: name, text });
      setName(''); setText('');
      await load();
    }catch(ex){
      alert(ex?.data?.message || 'Greška pri slanju komentara');
    }finally{ setBusy(false) }
  };

  const react = async (cid, val)=>{
    try{
      await API.post(`/api/public/comments/${cid}/react`, { reaction:Number(val) });
      await load();
    }catch(_){}
  };

  return (
    <div className="card">
      <h3 style={{marginTop:0}}>Komentari</h3>


      <div className="grid" style={{gap:12}}>
        {items.map(c=>{
          const created = fmtDate(c.createdAt || c.created_at);
          const likes   = c.likes  ?? 0;
          const dislikes= c.dislikes ?? 0;
          return (
            <div key={c.id} className="card" style={{padding:12}}>
              <div className="row" style={{justifyContent:'space-between', gap:8}}>
                <strong>{c.authorName || c.author_name || 'Anon'}</strong>
                <span className="badge">{created}</span>
              </div>
              <p style={{margin:'8px 0 10px 0'}}>{c.text}</p>
              <div className="row" style={{gap:8}}>
                <button className="btn" onClick={()=>react(c.id, 1)}>👍 {likes}</button>
                <button className="btn" onClick={()=>react(c.id, -1)}>👎 {dislikes}</button>
              </div>
            </div>
          );
        })}
        {items.length===0 && <p className="muted">Još nema komentara.</p>}
      </div>


      <form className="grid" style={{gap:8, marginTop:12}} onSubmit={submit}>
        <div className="row" style={{gap:8, flexWrap:'wrap'}}>
          <input
            style={{maxWidth:'260px'}}
            placeholder="Vaše ime"
            value={name}
            onChange={e=>setName(e.target.value)}
          />
          <input
            style={{flex:1, minWidth:260}}
            placeholder="Vaš komentar"
            value={text}
            onChange={e=>setText(e.target.value)}
          />
          <button className="btn brand" disabled={busy}>{busy?'Slanje…':'Pošalji'}</button>
        </div>
      </form>
    </div>
  );
}



    // EMS


    function EmsCategories(){
        const [items,setItems] = useState([]);
        const [q,setQ] = useState('');
        const [name,setName] = useState('');
        const [description,setDescription] = useState('');
        const [editId,setEditId] = useState(null);
        const [eName,setEName] = useState('');
        const [eDesc,setEDesc] = useState('');

        const load = async ()=>{
            const url = new URL('/api/ems/categories', window.location.origin);
            url.searchParams.set('page', 1);
            url.searchParams.set('limit', 200);
            if (q) url.searchParams.set('q', q);
            const r = await API.get(url.toString());
            setItems(r.items||r||[]);
        };
        useEffect(()=>{ load() },[q]);

        const create = async (e)=>{
            e.preventDefault();
            if (!name.trim() || !description.trim()) { alert('Naziv i opis su obavezni.'); return; }
            try{
                await API.post('/api/ems/categories', { name, description });
                setName(''); setDescription('');
                await load();
                alert('Kategorija kreirana.');
            }catch(ex){ alert(ex?.data?.message || 'Greška pri kreiranju'); }
        };

        const beginEdit = (c)=>{ setEditId(c.id); setEName(c.name); setEDesc(c.description||''); };
        const cancelEdit = ()=>{ setEditId(null); setEName(''); setEDesc(''); };
        const saveEdit = async (id)=>{
            try{
                await API.put('/api/ems/categories/'+id, { name:eName, description:eDesc });
                await load(); cancelEdit(); alert('Izmenjeno.');
            }catch(ex){ alert(ex?.data?.message || 'Greška pri izmeni'); }
        };
        const remove = async (c)=>{
            const cnt = c.eventsCount ?? c.events_count ?? 0;
            if (cnt>0) { alert('Brisanje nije dozvoljeno: postoje događaji u ovoj kategoriji.'); return; }
            if (!confirm('Obrisati kategoriju?')) return;
            try{ await API.del('/api/ems/categories/'+c.id); await load(); alert('Obrisano.'); }
            catch(ex){ alert(ex?.data?.message || 'Greška pri brisanju'); }
        };

        return (
            <div className="container page grid" style={{gap:16}}>
                <div className="card" style={{maxWidth:'780px'}}>
                    <h2>Nova kategorija</h2>
                    <form className="grid" style={{gap:12}} onSubmit={create}>
                        <div className="row" style={{gap:12}}>
                            <label className="field" style={{flex:1}}><input placeholder="Naziv" value={name} onChange={e=>setName(e.target.value)} required /></label>
                            <label className="field" style={{flex:2}}><input placeholder="Opis" value={description} onChange={e=>setDescription(e.target.value)} required /></label>
                        </div>
                        <button className="btn brand">Snimi</button>
                    </form>
                </div>

                <div className="card">
                    <div className="row" style={{justifyContent:'space-between'}}>
                        <h2 style={{margin:0}}>Postojeće kategorije</h2>
                        <label className="field"><input placeholder="Pretraga" value={q} onChange={e=>setQ(e.target.value)} /></label>
                    </div>
                    <table className="table">
                        <thead><tr><th>ID</th><th>Naziv</th><th>Opis</th><th>Događaja</th><th>Akcije</th></tr></thead>
                        <tbody>
                        {items.map(c=>{
                            const cnt = c.eventsCount ?? c.events_count ?? 0;
                            const editing = editId === c.id;
                            return (
                                <tr key={c.id}>
                                    <td>{c.id}</td>
                                    <td>{editing ? <input value={eName} onChange={e=>setEName(e.target.value)} /> : c.name}</td>
                                    <td>{editing ? <input value={eDesc} onChange={e=>setEDesc(e.target.value)} /> : <span className="muted">{c.description}</span>}</td>
                                    <td><span className="pill">{cnt}</span></td>
                                    <td>
                                        {editing ? (
                                            <div className="row">
                                                <button className="btn brand" onClick={()=>saveEdit(c.id)}>Sačuvaj</button>
                                                <button className="btn" onClick={cancelEdit}>Otkaži</button>
                                            </div>
                                        ) : (
                                            <div className="row">
                                                <button className="btn" onClick={()=>beginEdit(c)}>Izmeni</button>
                                                <button className="btn" disabled={cnt>0} title={cnt>0?'Postoje događaji u kategoriji':''} onClick={()=>remove(c)}>Obriši</button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

    function EmsEvents({ route, push }){
        const parseFromRoute = (r) => {
            const qs = new URLSearchParams(r.split('?')[1] || '');
            return {
                q: qs.get('q') || '',
                page: parseInt(qs.get('page') || '1')
            };
        };
        const init = parseFromRoute(route);

        const [q, setQ]       = useState(init.q);
        const [page, setPage] = useState(init.page);
        const [limit, setLimit] = useState(10);
        const [items, setItems] = useState([]);
        const [total, setTotal] = useState(0);


        useEffect(() => {
            const p = parseFromRoute(route);
            setQ(p.q);
            setPage(p.page);
        }, [route]);

        const load = async ()=>{
            const url = new URL('/api/ems/events', window.location.origin);
            url.searchParams.set('page', page);
            url.searchParams.set('limit', limit);
            if (q.trim()) url.searchParams.set('q', q.trim());
            const r = await API.get(url.toString());
            const arr = (r.items||r||[]).slice().sort((a,b)=> new Date(b.createdAt||b.created_at||0) - new Date(a.createdAt||a.created_at||0));
            setItems(arr);
            setTotal(r.total ?? arr.length);
        };
        useEffect(()=>{ load() },[q, page, limit]);

        const pages = Math.max(1, Math.ceil((total||0)/limit));
        const go = (p)=> {
            const np = Math.min(pages, Math.max(1,p));
            setPage(np);
            push('/ems/events?q='+encodeURIComponent(q)+'&page='+np);
        };

        const remove = async (ev)=>{
            if (!confirm('Obrisati događaj i sve njegove komentare?')) return;
            try{ await API.del('/api/ems/events/'+ev.id); await load(); alert('Događaj obrisan.'); }
            catch(ex){ alert(ex?.data?.message || 'Greška pri brisanju'); }
        };

        return (
            <div className="container page grid" style={{gap: 16}}>
                <div className="row" style={{justifyContent: 'space-between'}}>
                    <form
                        className="field"
                        onSubmit={(e) => {
                            e.preventDefault();
                            setPage(1);
                            push('/ems/events?q=' + encodeURIComponent(q) + '&page=1');
                        }}
                    >
                        <input placeholder="Pretraga događaja" value={q} onChange={e => setQ(e.target.value)}/>
                    </form>
                    <a className="btn brand" href="#/ems/events/new">Dodaj novi događaj</a>
                </div>

                <div className="card">
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Naslov</th>
                            <th>Autor</th>
                            <th>Kreiran</th>
                            <th>Akcije</th>
                        </tr>
                        </thead>
                        <tbody>
                        {items.map(ev => {
                            const author = (getAuthor(ev) || '').trim();
                            return (
                                <tr key={ev.id}>
                                    <td>
                                        <a href={'#/event/' + ev.id}>
                                            {ev.title}
                                        </a>
                                    </td>
                                    <td>{author || '—'}</td>

                                    <td>{fmtDate(ev.createdAt || ev.created_at)}</td>
                                    <td>
                                        <div className="row">
                                            <a className="btn" href={'#/ems/events/' + ev.id + '/edit'}>Izmeni</a>
                                            <button className="btn" onClick={() => remove(ev)}>Obriši</button>
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>

                    {pages > 1 && (
                        <div className="row" style={{justifyContent: 'center', marginTop: 8}}>
                            <div className="pagination">
                                <button onClick={() => go(1)} disabled={page === 1}>«</button>
                                <button onClick={() => go(page - 1)} disabled={page === 1}>‹</button>
                                <span className="pill">Strana {page} / {pages}</span>
                                <button onClick={() => go(page + 1)} disabled={page === pages}>›</button>
                                <button onClick={() => go(pages)} disabled={page === pages}>»</button>
                            </div>
                        </div>
                    )}
                </div>

            </div>
        );
    }



    function EmsEventForm({id, onSaved}) {
        const editing = !!id;
        const [cats, setCats] = useState([]);
        const [title, setTitle] = useState('');
        const [description, setDescription] = useState('');
        const [scheduledAt, setScheduledAt] = useState('2025-12-31T20:30:00');
        const [location, setLocation] = useState('');
        const [maxCapacity, setMaxCapacity] = useState('');
        const [categoryId, setCategoryId] = useState('');
        const [tags, setTags] = useState('');

        useEffect(() => {
            (async () => {
                const cs = await API.get('/api/public/categories');
                setCats(cs.items || cs || []);
                if (editing) {
                    const ev = await API.get('/api/ems/events/' + id);
                    setTitle(ev.title || '');
                    setDescription(ev.description || '');
                    const toLocal = (s) => {
                        if (!s) return '';
                        const d = new Date(s);
                        const pad = n => String(n).padStart(2, '0');
                        return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
                    };
                    setScheduledAt(toLocal(ev.scheduledAt || ev.scheduled_at) || '');
                    setLocation(ev.location || '');
                    setMaxCapacity(ev.maxCapacity ?? '');
                setCategoryId(String(ev.categoryId || ev.category_id || ''));
                setTags((ev.tags||[]).join(', '));
            }
        })() },[id]);

        const parseTags = (s)=> s.split(/[,;]+/).map(x=>x.trim()).filter(Boolean);

        const submit = async (e)=>{
            e.preventDefault();
            if (!title.trim() || !description.trim() || !scheduledAt.trim() || !location.trim() || !categoryId){ alert('Sva obavezna polja moraju biti popunjena.'); return; }
            const payload = {
                title, description, scheduledAt, location,
                maxCapacity: (maxCapacity===''||maxCapacity===null)? null : Number(maxCapacity),
                categoryId: Number(categoryId),
                tags: parseTags(tags)
            };
            try{
                if (editing){
                    await API.put('/api/ems/events/'+id, payload);
                    alert('Događaj izmenjen.');
                }else{
                    await API.post('/api/ems/events', payload);
                    alert('Događaj kreiran.');
                }
                onSaved?.();
            }catch(ex){
                alert(ex?.data?.message || 'Greška pri snimanju');
            }
        };

        return (
            <div className="container page">
                <div className="card" style={{maxWidth:'820px', margin:'12px auto'}}>
                    <h2>{editing? 'Izmena događaja':'Novi događaj'}</h2>
                    <form className="grid" style={{ gap: 12 }} onSubmit={submit}>
                        <label className="field" style={{padding:'0 12px'}}><input placeholder="Naslov" value={title} onChange={e=>setTitle(e.target.value)} required /></label>
                        <label className="field" style={{padding:'0 12px'}}><input placeholder="Opis" value={description} onChange={e=>setDescription(e.target.value)} required /></label>
                        <label className="field" style={{padding:'0 12px'}}><input placeholder="YYYY-MM-DDTHH:mm:ss" value={scheduledAt} onChange={e=>setScheduledAt(e.target.value)} required /></label>
                        <div className="row" style={{gap:12}}>
                            <label className="field" style={{flex:1, padding:'0 12px'}}><input placeholder="Lokacija" value={location} onChange={e=>setLocation(e.target.value)} required /></label>
                            <label className="field" style={{maxWidth:'180px', padding:'0 12px'}}><input type="number" placeholder="Max kapacitet (opciono)" value={maxCapacity} onChange={e=>setMaxCapacity(e.target.value)} /></label>
                        </div>
                        <label className="field" style={{padding:'0 12px'}}>
                            <select value={categoryId} onChange={e=>setCategoryId(e.target.value)} required style={{border:'none', outline:'none', padding:'8px 4px', background:'transparent', width:'100%'}}>
                                <option value="">Izaberi kategoriju…</option>
                                {cats.map(c=> <option key={c.id} value={c.id}>{c.name}</option>)}
                            </select>
                        </label>
                        <label className="field" style={{padding:'0 12px'}}><input placeholder="Tagovi (zarezom odvojeni)" value={tags} onChange={e=>setTags(e.target.value)} /></label>
                        <div className="row" style={{gap:8}}>
                            <button className="btn brand" type="submit">{editing?'Sačuvaj izmene':'Kreiraj'}</button>
                            <a className="btn" href="#/ems/events">Otkaži</a>
                        </div>
                    </form>
                </div>
            </div>
        );
    }



    function AdminUsers(){
        const [items,setItems] = React.useState([]);
        const [page,setPage]   = React.useState(1);
        const [total,setTotal] = React.useState(0);
        const [q,setQ]         = React.useState('');


        const [cFirst,setCFirst] = React.useState('');
        const [cLast,setCLast]   = React.useState('');
        const [cEmail,setCEmail] = React.useState('');
        const [cType,setCType]   = React.useState('EVENT_CREATOR');
        const [cPass,setCPass]   = React.useState('');
        const [cPass2,setCPass2] = React.useState('');
        const [cActive,setCActive] = React.useState(true);


        const [editingId,setEditingId] = React.useState(null);
        const [eFirst,setEFirst] = React.useState('');
        const [eLast,setELast]   = React.useState('');
        const [eEmail,setEEmail] = React.useState('');
        const [eType,setEType]   = React.useState('EVENT_CREATOR');

        const pageSize = 10;

        const load = async ()=>{
            const url = new URL('/api/ems/users', window.location.origin);
            url.searchParams.set('page', page);
            url.searchParams.set('limit', pageSize);
            if (q) url.searchParams.set('q', q);
            const r = await API.get(url.toString());
            const arr = r.items || r || [];
            setItems(arr);
            setTotal(r.total ?? arr.length);
        };
        React.useEffect(()=>{ load() }, [page, q]);


        const submitCreate = async (e)=>{
            e.preventDefault();
            if (!cFirst.trim() || !cLast.trim() || !cEmail.trim() || !cPass) { alert('Sva polja su obavezna.'); return; }
            if (cPass !== cPass2) { alert('Lozinke se ne poklapaju.'); return; }


            const created = await API.post('/api/ems/users', {
                firstName: cFirst, lastName: cLast, email: cEmail, password: cPass
            }).catch(ex => { throw (ex?.data?.message || ex); });


            let uid = created?.id;
            if (!uid) {
                try {
                    const r = await API.get('/api/ems/users?page=1&limit=10&q='+encodeURIComponent(cEmail));
                    const match = (r.items || r || []).find(u => (u.email||'').toLowerCase() === cEmail.toLowerCase());
                    if (match) uid = match.id;
                } catch(_) {}
            }


            if (uid && cType) {
                try {
                    await API.put(`/api/ems/users/${uid}`, {
                        firstName: cFirst, lastName: cLast, email: cEmail, type: cType
                    });
                } catch(_) {  }
            }


            if (uid && cType !== 'ADMIN') {
                try {
                    await API.req(`/api/ems/users/${uid}/status`, { method:'POST', body: JSON.stringify({ active: !!cActive }) });
                } catch(_) {
                    try {
                        await API.req(`/api/ems/users/${uid}/active`, { method:'POST', body: JSON.stringify({ active: !!cActive }) });
                    } catch(__) { }
                }
            }


            setCFirst(''); setCLast(''); setCEmail(''); setCType('EVENT_CREATOR');
            setCPass(''); setCPass2(''); setCActive(true);
            await load();
            alert('Korisnik je kreiran.');
        };



        const beginEdit = (u)=>{
            setEditingId(u.id);
            setEFirst(u.firstName ?? u.first_name ?? '');
            setELast(u.lastName ?? u.last_name ?? '');
            setEEmail(u.email ?? '');
            const t = (u.type ?? '').toString().toUpperCase();
            setEType(t.includes('ADMIN') ? 'ADMIN' : 'EVENT_CREATOR');
        };
        const cancelEdit = ()=>{
            setEditingId(null); setEFirst(''); setELast(''); setEEmail(''); setEType('EVENT_CREATOR');
        };
        const saveEdit = async (id)=>{
            if (!eFirst.trim() || !eLast.trim() || !eEmail.trim()) { alert('Popunite sva polja.'); return; }
            await API.put(`/api/ems/users/${id}`, {
                firstName: eFirst,
                lastName:  eLast,
                email:     eEmail,
                type:      eType
            });
            await load();
            cancelEdit();
            alert('Promene su sačuvane.');
        };




        const toggleActive = async (u)=>{
            const t = (u.type ?? '').toString().toUpperCase();
            if (t.includes('ADMIN')) return;

            const willActivate = !(u.active === true || u.active === 1 || u.status === 'ACTIVE');
            const ep = willActivate ? 'activate' : 'deactivate';
            await API.req(`/api/ems/users/${u.id}/${ep}`, { method:'PUT' });
            await load();
            alert(willActivate ? 'Korisnik je aktiviran.' : 'Korisnik je deaktiviran.');
        };



        const pages = Math.max(1, Math.ceil((total||0)/pageSize));
        const go = (p)=> setPage(Math.min(pages, Math.max(1,p)));

        return (
            <div className="container grid" style={{gap:16}}>

                <div className="card" style={{maxWidth:900}}>
                    <h2>Dodaj novog korisnika</h2>
                    <form className="grid" onSubmit={submitCreate} style={{gap:12}}>
                        <div className="row" style={{gap:12}}>
                            <label className="field" style={{flex:1}}><input placeholder="Ime" value={cFirst} onChange={e=>setCFirst(e.target.value)} required/></label>
                            <label className="field" style={{flex:1}}><input placeholder="Prezime" value={cLast} onChange={e=>setCLast(e.target.value)} required/></label>
                            <label className="field" style={{flex:2}}><input type="email" placeholder="Email" value={cEmail} onChange={e=>setCEmail(e.target.value)} required/></label>
                        </div>
                        <div className="row" style={{gap:12}}>
                            <label className="field" style={{flex:1}}>
                                <select value={cType} onChange={e=>setCType(e.target.value)} style={{border:'none', outline:'none', padding:'8px 4px', background:'transparent', width:'100%'}}>
                                    <option value="EVENT_CREATOR">EVENT_CREATOR</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                            </label>
                            <label className="field" style={{flex:1}}><input type="password" placeholder="Lozinka" value={cPass} onChange={e=>setCPass(e.target.value)} required/></label>
                            <label className="field" style={{flex:1}}><input type="password" placeholder="Potvrda lozinke" value={cPass2} onChange={e=>setCPass2(e.target.value)} required/></label>
                        </div>
                        <button className="btn brand">Dodaj korisnika</button>
                    </form>
                </div>


                <div className="card">
                    <div className="row">
                        <label className="field"><input placeholder="Pretraga (ime, prezime, email)..." value={q} onChange={e=>{ setQ(e.target.value); setPage(1); }} /></label>
                    </div>
                </div>


                <div className="card">
                    <table className="table">
                        <thead>
                        <tr>
                            <th style={{width:70}}>ID</th>
                            <th>Email</th>
                            <th>Ime</th>
                            <th>Prezime</th>
                            <th>Tip</th>
                            <th>Status</th>
                            <th style={{width:260}}>Akcije</th>
                        </tr>
                        </thead>
                        <tbody>
                        {items.map(u=>{
                            const t = (u.type ?? '').toString().toUpperCase();
                            const isAdmin = t.includes('ADMIN');
                            const statusActive = (u.active === 1 || u.active === true) || (u.status === 'ACTIVE');
                            const editing = editingId === u.id;
                            return (
                                <tr key={u.id}>
                                    <td>{u.id}</td>
                                    <td>{editing ? <input value={eEmail} onChange={e=>setEEmail(e.target.value)} /> : (u.email||'')}</td>
                                    <td>{editing ? <input value={eFirst} onChange={e=>setEFirst(e.target.value)} /> : (u.firstName ?? u.first_name)}</td>
                                    <td>{editing ? <input value={eLast}  onChange={e=>setELast(e.target.value)}  /> : (u.lastName ?? u.last_name)}</td>
                                    <td>
                                        {editing ? (
                                            <select value={eType} onChange={e=>setEType(e.target.value)}>
                                                <option value="EVENT_CREATOR">EVENT_CREATOR</option>
                                                <option value="ADMIN">ADMIN</option>
                                            </select>
                                        ) : t}
                                    </td>
                                    <td>
                  <span className="pill" style={{borderColor: statusActive?'#cde7d8':'#f5c2c7', color: statusActive?'#065f46':'#842029'}}>
                    {statusActive ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                                    </td>
                                    <td>
                                        {editing ? (
                                            <div className="row">
                                                <button className="btn brand" onClick={()=>saveEdit(u.id)}>Sačuvaj</button>
                                                <button className="btn" onClick={cancelEdit}>Otkaži</button>
                                            </div>
                                        ) : (
                                            <div className="row">
                                                <button className="btn" onClick={()=>beginEdit(u)}>Izmeni</button>
                                                <button
                                                    className="btn"
                                                    disabled={isAdmin}
                                                    title={isAdmin ? 'Admin je uvek aktivan' : (statusActive ? 'Deaktiviraj korisnika' : 'Aktiviraj korisnika')}
                                                    onClick={()=>toggleActive(u)}
                                                >
                                                    {isAdmin ? '—' : (statusActive ? 'Deaktiviraj' : 'Aktiviraj')}
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>

                    {pages>1 && (
                        <div className="row" style={{justifyContent:'center', marginTop:8}}>
                            <div className="pagination">
                                <button onClick={()=>go(1)} disabled={page===1}>«</button>
                                <button onClick={()=>go(page-1)} disabled={page===1}>‹</button>
                                <span className="pill">Strana {page} / {pages}</span>
                                <button onClick={()=>go(page+1)} disabled={page===pages}>›</button>
                                <button onClick={()=>go(pages)} disabled={page===pages}>»</button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        );
    }

    // AUTH
    function Login({ onSuccess }){
        const [email,setEmail] = useState('');
        const [password,setPassword] = useState('');
        const [err,setErr] = useState(null);
        const [busy,setBusy] = useState(false);

        const submit = async (e)=>{
            e.preventDefault(); setBusy(true); setErr(null);
            try{
                const me = await API.post('/api/auth/login',{ email, password });
                onSuccess?.(me);
            }catch(ex){
                setErr(ex?.data?.message || 'Greška pri prijavi');
            }finally{ setBusy(false) }
        };

        return (
            <div className="container page" style={{maxWidth:'520px'}}>
                <div className="card">
                    <h2>Prijava</h2>
                    <form className="grid" style={{gap:12}} onSubmit={submit}>
                        <label className="field" style={{padding:'0 12px'}}>
                            <input type="email" placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} required />
                        </label>
                        <label className="field" style={{padding:'0 12px'}}>
                            <input type="password" placeholder="Lozinka" value={password} onChange={e=>setPassword(e.target.value)} required />
                        </label>
                        {err && <div className="card" style={{borderColor:'#f5c2c7', background:'#fff1f2', color:'#842029'}}>{err}</div>}
                        <button className="btn accent" disabled={busy}>{busy?'Prijavljivanje…':'Prijavi se'}</button>
                    </form>
                </div>
            </div>
        );
    }

    //  NAV
    function Nav({ user, setUser, push }){
        const [open, setOpen] = useState(false);
        const [cats, setCats] = useState([]);
        const [q,setQ] = useState('');

        const isCreator = !!user && (user.userType === 'ADMIN' || user.userType === 'EVENT_CREATOR');
        const isAdmin = !!user && user.userType === 'ADMIN';

        useEffect(()=>{ API.get('/api/public/categories').then(r=> setCats(r.items||r||[])).catch(()=>setCats([])); },[]);

        const gotoCat = (id,e)=>{ e?.preventDefault(); setOpen(false); push('/events?categoryId='+id+'&page=1'); };
        const searchPublic = (e)=>{ e.preventDefault(); push('/events?q='+encodeURIComponent(q)+'&page=1'); };
        const searchEms = (e)=>{ e.preventDefault(); push('/ems/events?q='+encodeURIComponent(q)+'&page=1'); };

        useEffect(()=>{
            const on = (ev)=>{ if (!ev.target.closest('.dropdown')) setOpen(false); };
            document.addEventListener('click', on);
            return ()=> document.removeEventListener('click', on);
        },[]);

        const logout = async ()=>{
            try{ await API.post('/api/auth/logout'); }catch(_){}
            setUser(null);
            push('/');
        };

        return (
            <nav className="nav">
                <div className="nav-inner container">
                    <div className="nav-left">
                        {!isCreator ? (
                            <>
                                <a className="btn" href="#/">Home</a>
                                <a className="btn" href="#/most-visited">Najposećeniji</a>
                                <div className={'dropdown'+(open?' open':'')}>
                                    <button className="btn" onClick={()=>setOpen(!open)}>Kategorije ▾</button>
                                    <div className="dropdown-menu" role="menu" aria-label="Kategorije">
                                        {cats.map(c => (
                                            <a key={c.id} href="#" onClick={(e)=>gotoCat(c.id,e)}>{c.name}</a>
                                        ))}
                                    </div>
                                </div>
                                <form className="field" onSubmit={searchPublic} style={{marginLeft:'6px'}}>
                                    <input placeholder="Pretraga" value={q} onChange={e=>setQ(e.target.value)} />
                                </form>
                            </>
                        ) : (
                            <>
                                <a className="btn" href="#/ems/events">Događaji</a>
                                <a className="btn" href="#/ems/categories">Kategorije</a>
                                {isAdmin && <a className="btn" href="#/ems/users">Korisnici</a>}
                                <form className="field" onSubmit={searchEms} style={{marginLeft:'6px'}}>
                                    <input placeholder="Pretraga" value={q} onChange={e=>setQ(e.target.value)} />
                                </form>
                            </>
                        )}
                    </div>

                    <div className="nav-right">
                        {!user ? (
                            <a className="btn accent" href="#/login">Prijavi se</a>
                        ) : (
                            <>
                              <span className="profile-pill">
                                {(roleLabel(user) || 'Korisnik') + ': ' + displayName(user)}
                              </span>
                                <button className="btn accent" onClick={logout}>Odjavi se</button>
                            </>
                        )}
                    </div>
                </div>
            </nav>
        );
    }

    //  ROUTER/APP
    function useHashRoute(){
        const [route, setRoute] = useState(window.location.hash.slice(1) || '/');
        useEffect(()=>{ const on = ()=> setRoute(window.location.hash.slice(1) || '/'); window.addEventListener('hashchange', on); return ()=> window.removeEventListener('hashchange', on); },[]);
        const push = (r)=> { if (r===route) window.dispatchEvent(new HashChangeEvent('hashchange')); else window.location.hash = r };
        return [route, push];
    }

    function App(){
        const [route, push] = useHashRoute();
        const [user, setUser] = useState(null);
        const [loadingUser, setLoadingUser] = useState(true);

        useEffect(()=>{ (async()=>{
            try{
                const me = await API.get('/api/auth/me');
                setUser(normalizeUser(me));
            }catch(_){
                setUser(null); // 401 ce javljati kad niko nije ulogovan
            }finally{
                setLoadingUser(false);
            }
        })() },[]);

        if (loadingUser) return <div className="container page"><p>Učitavanje…</p></div>;

        return (
            <>
                <Nav user={user} setUser={setUser} push={push} />
                <MostReacted limit={3} />


                {(!user || route==='/') && route==='/' && <Home/>}
                {(!user || route==='/most-visited') && route==='/most-visited' && <MostVisited/>}
                {(!user || route.startsWith('/events')) && route.startsWith('/events') && (
                    <EventsList key={route} route={route} push={push} />
                )}
                {route.startsWith('/event/') && <EventDetails id={route.split('/')[2]} />}


                {route==='/login' && (
                    <Login
                        onSuccess={(me) => {
                            const u = normalizeUser(me);
                            setUser(u);
                            const isCreator = u.userType === 'ADMIN' || u.userType === 'EVENT_CREATOR';
                            push(isCreator ? '/ems/events' : '/');
                        }}
                    />
                )}


                {user && (user.userType === 'ADMIN' || user.userType === 'EVENT_CREATOR') && (
                    <>
                        {route.startsWith('/ems/events') && <EmsEvents route={route} push={push} />}
                        {route==='/ems/categories' && <EmsCategories/>}
                        {route.startsWith('/ems/events/') && route.endsWith('/edit') && (
                            <EmsEventForm id={route.split('/')[3]} onSaved={()=>push('/ems/events')} />
                        )}
                        {route==='/ems/events/new' && <EmsEventForm onSaved={()=>push('/ems/events')} />}
                        {user.userType === 'ADMIN' && route==='/ems/users' && <AdminUsers/>}
                    </>
                )}

            </>
        );
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<App/>);
</script>
</body>
</html>
